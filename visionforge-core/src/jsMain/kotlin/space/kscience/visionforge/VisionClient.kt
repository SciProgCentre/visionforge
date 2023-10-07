package space.kscience.visionforge

import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.w3c.dom.*
import org.w3c.dom.url.URL
import space.kscience.dataforge.context.*
import space.kscience.dataforge.meta.*
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.parseAsName
import space.kscience.visionforge.html.VisionTagConsumer
import space.kscience.visionforge.html.VisionTagConsumer.Companion.OUTPUT_CONNECT_ATTRIBUTE
import space.kscience.visionforge.html.VisionTagConsumer.Companion.OUTPUT_ENDPOINT_ATTRIBUTE
import space.kscience.visionforge.html.VisionTagConsumer.Companion.OUTPUT_FETCH_ATTRIBUTE
import space.kscience.visionforge.html.VisionTagConsumer.Companion.OUTPUT_NAME_ATTRIBUTE
import space.kscience.visionforge.html.VisionTagConsumer.Companion.OUTPUT_RENDERED
import kotlin.time.Duration.Companion.milliseconds

/**
 * A Kotlin-browser plugin that renders visions based on provided renderers and governs communication with the server.
 */
public class VisionClient : AbstractPlugin() {
    override val tag: PluginTag get() = Companion.tag
    private val visionManager: VisionManager by require(VisionManager)

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

    internal val renderers by lazy { context.gather<ElementVisionRenderer>(ElementVisionRenderer.TYPE).values }

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

    private val mutex = Mutex()

    private val changeCollector = VisionChangeBuilder()

    /**
     * Communicate vision property changed from rendering engine to model
     */
    public fun notifyPropertyChanged(visionName: Name, propertyName: Name, item: Meta?) {
        context.launch {
            mutex.withLock {
                changeCollector.propertyChanged(visionName, propertyName, item)
            }
        }
    }

    private val eventCollector by lazy {
        MutableSharedFlow<VisionEvent>(meta["feedback.eventCache"].int ?: 100)
    }


    /**
     * Send a custom feedback event
     */
    public suspend fun sendEvent(event: VisionEvent) {
        eventCollector.emit(event)
    }

    private fun renderVision(element: Element, name: Name, vision: Vision, outputMeta: Meta) {
        vision.setAsRoot(visionManager)
        val renderer = findRendererFor(vision) ?: error("Could not find renderer for ${vision::class}")
        renderer.render(element, name, vision, outputMeta)
    }

    private fun startVisionUpdate(element: Element, name: Name, vision: Vision?, outputMeta: Meta) {
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
                searchParams.append("name", name.toString())
            }

            logger.info { "Updating vision data from $wsUrl" }

            //Individual websocket for this vision
            WebSocket(wsUrl.toString()).apply {
                onmessage = { messageEvent ->
                    val stringData: String? = messageEvent.data as? String
                    if (stringData != null) {
                        val change: VisionChange = visionManager.jsonFormat.decodeFromString(
                            VisionChange.serializer(),
                            stringData
                        )

                        // If change contains root vision replacement, do it
                        change.vision?.let { vision ->
                            renderVision(element, name, vision, outputMeta)
                        }

                        logger.debug { "Got update $change for output with name $name" }
                        if (vision == null) error("Can't update vision because it is not loaded.")
                        vision.update(change)
                    } else {
                        logger.error { "WebSocket message data is not a string" }
                    }
                }


                //Backward change propagation
                var feedbackJob: Job? = null

                //Feedback changes aggregation time in milliseconds
                val feedbackAggregationTime = meta["feedback.aggregationTime"]?.int ?: 300

                onopen = {
                    feedbackJob = visionManager.context.launch {
                        while (isActive) {
                            delay(feedbackAggregationTime.milliseconds)
                            val change = changeCollector[name] ?: continue
                            if (!change.isEmpty()) {
                                mutex.withLock {
                                    send(change.toJsonString(visionManager))
                                    change.reset()
                                }
                            }
//                            // take channel for given vision name
//                            eventCollector[name]?.let { channel ->
//                                for (e in channel) {
//                                    send(visionManager.jsonFormat.encodeToString(VisionEvent.serializer(), e))
//                                }
//                            }
                        }
                    }
                    logger.info { "WebSocket feedback channel established for output '$name'" }
                }

                onclose = {
                    feedbackJob?.cancel()
                    logger.info { "WebSocket feedback channel closed for output '$name'" }
                }
                onerror = {
                    feedbackJob?.cancel()
                    logger.error { "WebSocket feedback channel error for output '$name'" }
                }
            }
        }
    }

    /**
     * Fetch from server and render a vision, described in a given with [VisionTagConsumer.OUTPUT_CLASS] class.
     */
    public fun renderVisionIn(element: Element) {
        if (!element.classList.contains(VisionTagConsumer.OUTPUT_CLASS)) error("The element $element is not an output element")
        val name = resolveName(element)?.parseAsName() ?: error("The element is not a vision output")

        if (element.attributes[OUTPUT_RENDERED]?.value == "true") {
            logger.info { "VF output in element $element is already rendered" }
            return
        } else {
            logger.info { "Rendering VF output with name $name" }
        }

        val outputMeta = element.getEmbeddedData(VisionTagConsumer.OUTPUT_META_CLASS)?.let {
            VisionManager.defaultJson.decodeFromString(MetaSerializer, it).also {
                logger.info { "Output meta for $name: $it" }
            }
        } ?: Meta.EMPTY

        when {
            // fetch data if the path is provided
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
                    searchParams.append("name", name.toString())
                }

                logger.info { "Fetching vision data from $fetchUrl" }
                window.fetch(fetchUrl).then { response ->
                    if (response.ok) {
                        response.text().then { text ->
                            val vision = visionManager.decodeFromString(text)
                            renderVision(element, name, vision, outputMeta)
                            startVisionUpdate(element, name, vision, outputMeta)
                        }
                    } else {
                        logger.error { "Failed to fetch initial vision state from $fetchUrl" }
                    }
                }
            }

            // use embedded data if it is available
            element.getElementsByClassName(VisionTagConsumer.OUTPUT_DATA_CLASS).length > 0 -> {
                //Getting embedded vision data
                val embeddedVision = element.getEmbeddedData(VisionTagConsumer.OUTPUT_DATA_CLASS)!!.let {
                    visionManager.decodeFromString(it)
                }
                logger.info { "Found embedded vision for output with name $name" }
                renderVision(element, name, embeddedVision, outputMeta)
                startVisionUpdate(element, name, embeddedVision, outputMeta)
            }

            //Try to load vision via websocket
            element.attributes[OUTPUT_CONNECT_ATTRIBUTE] != null -> {
                startVisionUpdate(element, name, null, outputMeta)
            }

            else -> error("No embedded vision data / fetch url for $name")
        }
        element.setAttribute(OUTPUT_RENDERED, "true")
    }

    override fun content(target: String): Map<Name, Any> = if (target == ElementVisionRenderer.TYPE) {
        listOf(
            numberVisionRenderer(this),
            textVisionRenderer(this),
            formVisionRenderer(this)
        ).associateByName()
    } else super.content(target)

    public companion object : PluginFactory<VisionClient> {
        override fun build(context: Context, meta: Meta): VisionClient = VisionClient()

        override val tag: PluginTag = PluginTag(name = "vision.client.js", group = PluginTag.DATAFORGE_GROUP)
    }
}

public fun VisionClient.notifyPropertyChanged(visionName: Name, propertyName: String, item: Meta?) {
    notifyPropertyChanged(visionName, propertyName.parseAsName(true), item)
}

public fun VisionClient.notifyPropertyChanged(visionName: Name, propertyName: String, item: Number) {
    notifyPropertyChanged(visionName, propertyName.parseAsName(true), Meta(item))
}

public fun VisionClient.notifyPropertyChanged(visionName: Name, propertyName: String, item: String) {
    notifyPropertyChanged(visionName, propertyName.parseAsName(true), Meta(item))
}

public fun VisionClient.notifyPropertyChanged(visionName: Name, propertyName: String, item: Boolean) {
    notifyPropertyChanged(visionName, propertyName.parseAsName(true), Meta(item))
}

public fun VisionClient.sendEvent(visionName: Name, event: MetaRepr): Unit {
    context.launch {
        sendEvent(VisionMetaEvent(visionName, event.toMeta()))
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
    logger.info { "Finished search for outputs. Found ${elements.length} items" }
    elements.asList().forEach { child ->
        renderVisionIn(child)
    }
}

/**
 * Render all visions in an element with a given [id]
 */
public fun VisionClient.renderAllVisionsById(document: Document, id: String): Unit {
    val element = document.getElementById(id)
    if (element != null) {
        renderAllVisionsIn(element)
    } else {
        logger.warn { "Element with id $id not found" }
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
    private val client = context.request(VisionClient)

    override fun start(document: Document, state: Map<String, Any>) {
        context.logger.info {
            "Starting VisionClient with renderers: ${
                client.renderers.joinToString(
                    prefix = "\n\t",
                    separator = "\n\t"
                ) { it.name.toString() }
            }"
        }
        val element = document.body ?: error("Document does not have a body")
        client.renderAllVisionsIn(element)
    }
}


/**
 * Create a vision client context and render all visions on the page.
 */
public fun runVisionClient(contextBuilder: ContextBuilder.() -> Unit) {
    Global.logger.info { "Starting VisionForge context" }

    val context = Context("VisionForge") {
        plugin(VisionClient)
        contextBuilder()
    }

    startApplication {
        VisionClientApplication(context)
    }
}