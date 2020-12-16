package hep.dataforge.vision.client

import hep.dataforge.context.*
import hep.dataforge.meta.Meta
import hep.dataforge.meta.MetaSerializer
import hep.dataforge.vision.Vision
import hep.dataforge.vision.VisionChange
import hep.dataforge.vision.VisionManager
import hep.dataforge.vision.html.VisionTagConsumer
import hep.dataforge.vision.html.VisionTagConsumer.Companion.OUTPUT_CONNECT_ATTRIBUTE
import hep.dataforge.vision.html.VisionTagConsumer.Companion.OUTPUT_ENDPOINT_ATTRIBUTE
import hep.dataforge.vision.html.VisionTagConsumer.Companion.OUTPUT_FETCH_ATTRIBUTE
import hep.dataforge.vision.html.VisionTagConsumer.Companion.OUTPUT_NAME_ATTRIBUTE
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.Element
import org.w3c.dom.WebSocket
import org.w3c.dom.asList
import org.w3c.dom.get
import org.w3c.dom.url.URL
import kotlin.collections.HashMap
import kotlin.collections.forEach
import kotlin.collections.maxByOrNull
import kotlin.collections.set
import kotlin.reflect.KClass

public class VisionClient : AbstractPlugin() {
    override val tag: PluginTag get() = Companion.tag
    private val visionManager: VisionManager by require(VisionManager)

    private val visionMap = HashMap<Element, Vision>()

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

    private fun findRendererFor(vision: Vision): ElementVisionRenderer? =
        getRenderers().maxByOrNull { it.rateVision(vision) }

    private fun Element.getEmbeddedData(className: String): String? = getElementsByClassName(className)[0]?.innerHTML

    private fun Element.getFlag(attribute: String): Boolean = attributes[attribute]?.value != null

    private fun renderVision(element: Element, vision: Vision?, outputMeta: Meta) {
        if (vision != null) {
            visionMap[element] = vision
            val renderer = findRendererFor(vision) ?: error("Could nof find renderer for $vision")
            renderer.render(element, vision, outputMeta)
        }
    }

    /**
     * Fetch from server and render a vision, described in a given with [VisionTagConsumer.OUTPUT_CLASS] class.
     */
    public fun renderVisionAt(element: Element) {
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

        if (embeddedVision != null) {
            logger.info { "Found embedded vision for output with name $name" }
            renderVision(element, embeddedVision, outputMeta)
        }

        val endpoint = resolveEndpoint(element)
        logger.info { "Vision server is resolved to $endpoint" }

        element.attributes[OUTPUT_FETCH_ATTRIBUTE]?.let {

            val fetchUrl = URL(endpoint).apply {
                searchParams.append("name", name)
                pathname += "/vision"
            }

            logger.info { "Fetching vision data from $fetchUrl" }
            window.fetch(fetchUrl).then { response ->
                if (response.ok) {
                    response.text().then { text ->
                        val vision = visionManager.decodeFromString(text)
                        renderVision(element, vision, outputMeta)
                    }
                } else {
                    logger.error { "Failed to fetch initial vision state from $endpoint" }
                }

            }
        }

        element.attributes[OUTPUT_CONNECT_ATTRIBUTE]?.let {

            val wsUrl = URL(endpoint).apply {
                pathname += "/ws"
                protocol = "ws"
                searchParams.append("name", name)
            }

            logger.info { "Updating vision data from $wsUrl" }

            val ws = WebSocket(wsUrl.toString()).apply {
                onmessage = { messageEvent ->
                    val stringData: String? = messageEvent.data as? String
                    if (stringData != null) {
                        val dif = visionManager.jsonFormat.decodeFromString(
                            VisionChange.serializer(),
                            stringData
                        )
                        logger.debug { "Got update $dif for output with name $name" }
                        visionMap[element]?.update(dif)
                            ?: console.info("Target vision for element $element with name $name not found")
                    } else {
                        console.error ("WebSocket message data is not a string")
                    }
                }
                onopen = {
                    console.info("WebSocket update channel established for output '$name'")
                }
                onclose = {
                    console.info("WebSocket update channel closed for output '$name'")
                }
                onerror = {
                    console.error("WebSocket update channel error for output '$name'")
                }
            }
        }
    }

    public companion object : PluginFactory<VisionClient> {

        override fun invoke(meta: Meta, context: Context): VisionClient = VisionClient()

        override val tag: PluginTag = PluginTag(name = "vision.client", group = PluginTag.DATAFORGE_GROUP)

        override val type: KClass<out VisionClient> = VisionClient::class
    }
}

/**
 * Fetch and render visions for all elements with [VisionTagConsumer.OUTPUT_CLASS] class inside given [element].
 */
public fun VisionClient.renderAllVisionsAt(element: Element) {
    val elements = element.getElementsByClassName(VisionTagConsumer.OUTPUT_CLASS)
    console.info("Finished search for outputs. Found ${elements.length} items")
    elements.asList().forEach { child ->
        renderVisionAt(child)
    }
}

/**
 * Fetch visions from the server for all elements with [VisionTagConsumer.OUTPUT_CLASS] class in the document body
 */
public fun VisionClient.renderAllVisions() {
    val element = document.body ?: error("Document does not have a body")
    renderAllVisionsAt(element)
}