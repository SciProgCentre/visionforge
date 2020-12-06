package hep.dataforge.vision.client

import hep.dataforge.context.*
import hep.dataforge.meta.Meta
import hep.dataforge.vision.Vision
import hep.dataforge.vision.VisionManager
import hep.dataforge.vision.html.HtmlOutputScope
import hep.dataforge.vision.html.HtmlOutputScope.Companion.OUTPUT_ENDPOINT_ATTRIBUTE
import hep.dataforge.vision.html.HtmlOutputScope.Companion.OUTPUT_NAME_ATTRIBUTE
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.Element
import org.w3c.dom.WebSocket
import org.w3c.dom.asList
import org.w3c.dom.get
import org.w3c.dom.url.URL
import kotlin.reflect.KClass

public class VisionClient : AbstractPlugin() {
    override val tag: PluginTag get() = Companion.tag
    private val visionManager: VisionManager by require(VisionManager)

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

    public fun findRendererFor(vision: Vision): ElementVisionRenderer? = getRenderers().maxByOrNull { it.rateVision(vision) }

    /**
     * Fetch from server and render a vision, described in a given with [HtmlOutputScope.OUTPUT_CLASS] class.
     */
    public fun fetchAndRenderVision(element: Element, requestUpdates: Boolean = true) {
        val name = resolveName(element) ?: error("The element is not a vision output")
        console.info("Found DF output with name $name")
        if (!element.classList.contains(HtmlOutputScope.OUTPUT_CLASS)) error("The element $element is not an output element")
        val endpoint = resolveEndpoint(element)
        console.info("Vision server is resolved to $endpoint")

        val fetchUrl = URL(endpoint).apply {
            searchParams.append("name", name)
            pathname += "/vision"
        }

        console.info("Fetching vision data from $fetchUrl")
        window.fetch(fetchUrl).then { response ->
            if (response.ok) {
                response.text().then { text ->
                    val vision = visionManager.decodeFromString(text)

                    val renderer = findRendererFor(vision) ?: error("Could nof find renderer for $vision")
                    renderer.render(element, vision)
                    if (requestUpdates) {
                        val wsUrl = URL(endpoint).apply {
                            pathname += "/ws"
                            protocol = "ws"
                            searchParams.append("name", name)
                        }
                        val ws = WebSocket(wsUrl.toString()).apply {
                            onmessage = { messageEvent ->
                                val stringData: String? = messageEvent.data as? String
                                if (stringData != null) {
                                    val update = visionManager.decodeFromString(text)
                                    vision.update(update)
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
            } else {
                console.error("Failed to fetch initial vision state from $endpoint")
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
 * Fetch and render visions for all elements with [HtmlOutputScope.OUTPUT_CLASS] class inside given [element].
 */
public fun VisionClient.fetchVisionsInChildren(element: Element, requestUpdates: Boolean = true) {
    val elements = element.getElementsByClassName(HtmlOutputScope.OUTPUT_CLASS)
    console.info("Finished search for outputs. Found ${elements.length} items")
    elements.asList().forEach { child ->
        fetchAndRenderVision(child, requestUpdates)
    }
}

/**
 * Fetch visions from the server for all elements with [HtmlOutputScope.OUTPUT_CLASS] class in the document body
 */
public fun VisionClient.fetchAndRenderAllVisions(requestUpdates: Boolean = true){
    val element = document.body ?: error("Document does not have a body")
    fetchVisionsInChildren(element, requestUpdates)
}