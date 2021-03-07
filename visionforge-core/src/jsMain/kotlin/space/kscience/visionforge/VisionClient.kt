package space.kscience.visionforge

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.Element
import org.w3c.dom.WebSocket
import org.w3c.dom.asList
import org.w3c.dom.get
import org.w3c.dom.url.URL
import space.kscience.dataforge.context.*
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.MetaSerializer
import space.kscience.visionforge.html.VisionTagConsumer
import space.kscience.visionforge.html.VisionTagConsumer.Companion.OUTPUT_CONNECT_ATTRIBUTE
import space.kscience.visionforge.html.VisionTagConsumer.Companion.OUTPUT_ENDPOINT_ATTRIBUTE
import space.kscience.visionforge.html.VisionTagConsumer.Companion.OUTPUT_FETCH_ATTRIBUTE
import space.kscience.visionforge.html.VisionTagConsumer.Companion.OUTPUT_NAME_ATTRIBUTE
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

        element.attributes[OUTPUT_FETCH_ATTRIBUTE]?.let { attr ->

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
                        renderVision(element, vision, outputMeta)
                    }
                } else {
                    logger.error { "Failed to fetch initial vision state from $fetchUrl" }
                }

            }
        }

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

            val ws = WebSocket(wsUrl.toString()).apply {
                onmessage = { messageEvent ->
                    val stringData: String? = messageEvent.data as? String
                    if (stringData != null) {
                        val change = visionManager.jsonFormat.decodeFromString(
                            VisionChange.serializer(),
                            stringData
                        )

                        if (change.vision != null) {
                            renderVision(element, change.vision, outputMeta)
                        }

                        logger.debug { "Got update $change for output with name $name" }
                        visionMap[element]?.update(change)
                            ?: console.info("Target vision for element $element with name $name not found")
                    } else {
                        console.error("WebSocket message data is not a string")
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