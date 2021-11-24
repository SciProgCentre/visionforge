package space.kscience.visionforge

import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.w3c.dom.*
import org.w3c.dom.url.URL
import space.kscience.dataforge.context.*
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.MetaSerializer
import space.kscience.visionforge.html.RENDER_FUNCTION_NAME
import space.kscience.visionforge.html.VisionTagConsumer
import space.kscience.visionforge.html.VisionTagConsumer.Companion.OUTPUT_CONNECT_ATTRIBUTE
import space.kscience.visionforge.html.VisionTagConsumer.Companion.OUTPUT_ENDPOINT_ATTRIBUTE
import space.kscience.visionforge.html.VisionTagConsumer.Companion.OUTPUT_FETCH_ATTRIBUTE
import space.kscience.visionforge.html.VisionTagConsumer.Companion.OUTPUT_NAME_ATTRIBUTE
import kotlin.reflect.KClass
import kotlin.time.Duration.Companion.milliseconds

public class VisionClient : AbstractPlugin() {
    override val tag: PluginTag get() = Companion.tag
    private val visionManager: VisionManager by require(VisionManager)

    //private val visionMap = HashMap<Element, Vision>()

    /**
     * Up-going tree traversal in search for endpoint attribute
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

    private fun getRenderers() = context.gather<ElementVisionRenderer>(ElementVisionRenderer.TYPE).values

    private fun findRendererFor(vision: Vision): ElementVisionRenderer? {
        return getRenderers().mapNotNull {
            val rating = it.rateVision(vision)
            if (rating > 0) {
                rating to it
            } else {
                null
            }
        }.maxByOrNull { it.first }?.second
    }

    private fun Element.getEmbeddedData(className: String): String? = getElementsByClassName(className)[0]?.innerHTML

    private fun Element.getFlag(attribute: String): Boolean = attributes[attribute]?.value != null

    private fun renderVision(name: String, element: Element, vision: Vision?, outputMeta: Meta) {
        if (vision != null) {
            val renderer = findRendererFor(vision) ?: error("Could nof find renderer for $vision")
            renderer.render(element, vision, outputMeta)

            element.attributes[OUTPUT_CONNECT_ATTRIBUTE]?.let { attr ->
                val wsUrl = if (attr.value.isBlank() || attr.value == "auto") {
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

                    onopen = {
                        feedbackJob = vision.flowChanges(
                            visionManager,
                            300.milliseconds
                        ).onEach { change ->
                            send(visionManager.encodeToString(change))
                        }.launchIn(visionManager.context)

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
        val name = resolveName(element) ?: error("The element is not a vision output")
        logger.info { "Found DF output with name $name" }
        if (!element.classList.contains(VisionTagConsumer.OUTPUT_CLASS)) error("The element $element is not an output element")


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

                val fetchUrl = if (attr.value.isBlank() || attr.value == "auto") {
                    val endpoint = resolveEndpoint(element)
                    logger.info { "Vision server is resolved to $endpoint" }
                    URL(endpoint).apply {
                        pathname += "/vision"
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
    }

    public companion object : PluginFactory<VisionClient> {

        override fun invoke(meta: Meta, context: Context): VisionClient = VisionClient()

        override val tag: PluginTag = PluginTag(name = "vision.client", group = PluginTag.DATAFORGE_GROUP)

        override val type: KClass<out VisionClient> = VisionClient::class
    }
}


private fun whenDocumentLoaded(block: Document.() -> Unit): Unit {
    if (document.readyState == DocumentReadyState.COMPLETE) {
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

/**
 * Create a vision client context and render all visions on the page.
 */
public fun runVisionClient(contextBuilder: ContextBuilder.() -> Unit) {
    console.info("Starting VisionForge context")
    val context = Context("VisionForge", contextBuilder)
    val visionClient = context.fetch(VisionClient)
    window.asDynamic()[RENDER_FUNCTION_NAME] = visionClient::renderAllVisionsById

    visionClient.renderAllVisions()
}