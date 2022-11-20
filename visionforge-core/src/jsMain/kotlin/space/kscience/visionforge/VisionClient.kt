package space.kscience.visionforge

import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.w3c.dom.*
import org.w3c.dom.url.URL
import space.kscience.dataforge.context.*
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.MetaSerializer
import space.kscience.dataforge.meta.get
import space.kscience.dataforge.meta.int
import space.kscience.dataforge.names.Name
import space.kscience.visionforge.html.VisionTagConsumer
import space.kscience.visionforge.html.VisionTagConsumer.Companion.OUTPUT_CONNECT_ATTRIBUTE
import space.kscience.visionforge.html.VisionTagConsumer.Companion.OUTPUT_ENDPOINT_ATTRIBUTE
import space.kscience.visionforge.html.VisionTagConsumer.Companion.OUTPUT_FETCH_ATTRIBUTE
import space.kscience.visionforge.html.VisionTagConsumer.Companion.OUTPUT_NAME_ATTRIBUTE
import space.kscience.visionforge.html.VisionTagConsumer.Companion.OUTPUT_RENDERED
import kotlin.reflect.KClass
import kotlin.time.Duration.Companion.milliseconds

/**
 * A Kotlin-browser plugin that renders visions based on provided renderers and governs communication with the server.
 */
public class VisionClient : AbstractPlugin() {
    override val tag: PluginTag get() = Companion.tag
    private val visionManager: VisionManager by require(VisionManager)

    //private val visionMap = HashMap<Element, Vision>()

    /**
     * Up-going tree traversal in search for endpoint attribute. If element is null, return window URL
     */
    private fun resolveEndpoint(element: Element?): String {
        if (element == null) return window.location.href
        val attribute = element.attributes[OUTPUT_ENDPOINT_ATTRIBUTE]
        return attribute?.value ?: resolveEndpoint(element.parentElement)
    }

    private fun resolveName(element: Element): String? {
        val attribute = element.attributes[OUTPUT_NAME_ATTRIBUTE]
        return attribute?.value
    }

    private val renderers by lazy { context.gather<ElementVisionRenderer>(ElementVisionRenderer.TYPE).values }

    private fun findRendererFor(vision: Vision): ElementVisionRenderer? = renderers.mapNotNull {
        val rating = it.rateVision(vision)
        if (rating > 0) {
            rating to it
        } else {
            null
        }
    }.maxByOrNull { it.first }?.second

    private fun Element.getEmbeddedData(className: String): String? = getElementsByClassName(className)[0]?.innerHTML

    private fun Element.getFlag(attribute: String): Boolean = attributes[attribute]?.value != null


    private val changeCollector = VisionChangeBuilder()

    public fun visionPropertyChanged(visionName: Name, propertyName: Name, item: Meta?) {
        changeCollector.propertyChanged(visionName, propertyName, item)
    }

    public fun visionChanged(name: Name?, child: Vision?) {
        changeCollector.setChild(name, child)
    }

    private fun renderVision(name: String, element: Element, vision: Vision?, outputMeta: Meta) {
        if (vision != null) {
            vision.setAsRoot(visionManager)
            val renderer = findRendererFor(vision)
                ?: error("Could not find renderer for ${vision::class}")
            renderer.render(element, vision, outputMeta)

            element.attributes[OUTPUT_CONNECT_ATTRIBUTE]?.let { attr ->
                val wsUrl = if (attr.value.isBlank() || attr.value == VisionTagConsumer.AUTO_DATA_ATTRIBUTE) {
                    val endpoint = resolveEndpoint(element)
                    logger.info { "Vision server is resolved to $endpoint" }
                    URL(endpoint).apply {
                        pathname += "/ws"
                    }
                } else {
                    URL(attr.value)
                }.apply {
                    protocol = "ws"
                    searchParams.append("name", name)
                }

                logger.info { "Updating vision data from $wsUrl" }

                //Individual websocket for this element
                WebSocket(wsUrl.toString()).apply {
                    onmessage = { messageEvent ->
                        val stringData: String? = messageEvent.data as? String
                        if (stringData != null) {
                            val change: VisionChange = visionManager.jsonFormat.decodeFromString(
                                VisionChange.serializer(),
                                stringData
                            )

                            if (change.vision != null) {
                                renderer.render(element, vision, outputMeta)
                            }

                            logger.debug { "Got update $change for output with name $name" }
                            vision.update(change)
                        } else {
                            console.error("WebSocket message data is not a string")
                        }
                    }


                    //Backward change propagation
                    var feedbackJob: Job? = null

                    //Feedback changes aggregation time in milliseconds
                    val feedbackAggregationTime = meta["aggregationTime"]?.int ?: 300

                    onopen = {
                        feedbackJob = visionManager.context.launch {
                            delay(feedbackAggregationTime.milliseconds)
                            if (!changeCollector.isEmpty()) {
                                send(visionManager.encodeToString(changeCollector.deepCopy(visionManager)))
                                changeCollector.reset()
                            }
                        }
                        console.info("WebSocket update channel established for output '$name'")
                    }

                    onclose = {
                        feedbackJob?.cancel()
                        console.info("WebSocket update channel closed for output '$name'")
                    }
                    onerror = {
                        feedbackJob?.cancel()
                        console.error("WebSocket update channel error for output '$name'")
                    }
                }
            }
        }
    }

    /**
     * Fetch from server and render a vision, described in a given with [VisionTagConsumer.OUTPUT_CLASS] class.
     */
    public fun renderVisionIn(element: Element) {
        if (!element.classList.contains(VisionTagConsumer.OUTPUT_CLASS)) error("The element $element is not an output element")
        val name = resolveName(element) ?: error("The element is not a vision output")

        if (element.attributes[OUTPUT_RENDERED]?.value == "true") {
            logger.info { "VF output in element $element is already rendered" }
            return
        } else {
            logger.info { "Rendering VF output with name $name" }
        }

        val outputMeta = element.getEmbeddedData(VisionTagConsumer.OUTPUT_META_CLASS)?.let {
            VisionManager.defaultJson.decodeFromString(MetaSerializer, it)
        } ?: Meta.EMPTY

        //Trying to render embedded vision
        val embeddedVision = element.getEmbeddedData(VisionTagConsumer.OUTPUT_DATA_CLASS)?.let {
            visionManager.decodeFromString(it)
        }

        when {
            embeddedVision != null -> {
                logger.info { "Found embedded vision for output with name $name" }
                renderVision(name, element, embeddedVision, outputMeta)
            }

            element.attributes[OUTPUT_FETCH_ATTRIBUTE] != null -> {
                val attr = element.attributes[OUTPUT_FETCH_ATTRIBUTE]!!

                val fetchUrl = if (attr.value.isBlank() || attr.value == VisionTagConsumer.AUTO_DATA_ATTRIBUTE) {
                    val endpoint = resolveEndpoint(element)
                    logger.info { "Vision server is resolved to $endpoint" }
                    URL(endpoint).apply {
                        pathname += "/data"
                    }
                } else {
                    URL(attr.value)
                }.apply {
                    searchParams.append("name", name)
                }

                logger.info { "Fetching vision data from $fetchUrl" }
                window.fetch(fetchUrl).then { response ->
                    if (response.ok) {
                        response.text().then { text ->
                            val vision = visionManager.decodeFromString(text)
                            renderVision(name, element, vision, outputMeta)
                        }
                    } else {
                        logger.error { "Failed to fetch initial vision state from $fetchUrl" }
                    }
                }
            }

            else -> error("No embedded vision data / fetch url for $name")
        }
        element.setAttribute(OUTPUT_RENDERED, "true")
    }

    override fun content(target: String): Map<Name, Any> = if (target == ElementVisionRenderer.TYPE) mapOf(
        numberVisionRenderer.name to numberVisionRenderer,
        textVisionRenderer.name to textVisionRenderer,
        formVisionRenderer.name to formVisionRenderer
    ) else super.content(target)

    public companion object : PluginFactory<VisionClient> {
        override fun build(context: Context, meta: Meta): VisionClient = VisionClient()

        override val tag: PluginTag = PluginTag(name = "vision.client", group = PluginTag.DATAFORGE_GROUP)

        override val type: KClass<out VisionClient> = VisionClient::class
    }
}


private fun whenDocumentLoaded(block: Document.() -> Unit): Unit {
    if (document.body != null) {
        block(document)
    } else {
        document.addEventListener("DOMContentLoaded", { block(document) })
    }
}

/**
 * Fetch and render visions for all elements with [VisionTagConsumer.OUTPUT_CLASS] class inside given [element].
 */
public fun VisionClient.renderAllVisionsIn(element: Element) {
    val elements = element.getElementsByClassName(VisionTagConsumer.OUTPUT_CLASS)
    console.info("Finished search for outputs. Found ${elements.length} items")
    elements.asList().forEach { child ->
        renderVisionIn(child)
    }
}

/**
 * Render all visions in an element with a given [id]
 */
public fun VisionClient.renderAllVisionsById(id: String): Unit = whenDocumentLoaded {
    val element = getElementById(id)
    if (element != null) {
        renderAllVisionsIn(element)
    } else {
        console.warn("Element with id $id not found")
    }
}


/**
 * Fetch visions from the server for all elements with [VisionTagConsumer.OUTPUT_CLASS] class in the document body
 */
public fun VisionClient.renderAllVisions(): Unit = whenDocumentLoaded {
    val element = body ?: error("Document does not have a body")
    renderAllVisionsIn(element)
}

public class VisionClientApplication(public val context: Context) : Application {
    private val client = context.fetch(VisionClient)

    override fun start(document: Document, state: Map<String, Any>) {
        console.info("Starting Vision Client")
        val element = document.body ?: error("Document does not have a body")
        client.renderAllVisionsIn(element)
    }
}


/**
 * Create a vision client context and render all visions on the page.
 */
public fun runVisionClient(contextBuilder: ContextBuilder.() -> Unit) {
    console.info("Starting VisionForge context")

    val context = Context("VisionForge") {
        plugin(VisionClient)
        contextBuilder()
    }

    startApplication {
        VisionClientApplication(context)
    }
}