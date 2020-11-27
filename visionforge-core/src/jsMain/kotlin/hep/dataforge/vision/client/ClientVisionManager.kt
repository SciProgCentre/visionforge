package hep.dataforge.vision.client

import hep.dataforge.context.Context
import hep.dataforge.context.Global
import hep.dataforge.vision.VisionManager
import hep.dataforge.vision.html.HtmlOutputScope
import kotlinx.browser.window
import org.w3c.dom.Element
import org.w3c.dom.get
import org.w3c.dom.url.URL

@JsExport
public class ClientVisionManager {
    private val visionForgeContext: Context = Global.context("client") {
        plugin(VisionManager)
    }

    private val visionManager: VisionManager = visionForgeContext.plugins.fetch(VisionManager)

    /**
     * Up-going tree traversal in search for endpoint attribute
     */
    private fun resolveEndpoint(element: Element?): String {
        if(element == null) return DEFAULT_ENDPOINT
        val attribute = element.attributes[OUTPUT_ENDPOINT_ATTRIBUTE]
        return attribute?.value ?: resolveEndpoint(element.parentElement)
    }

    public fun renderVision(element: Element){
        if(!element.classList.contains(HtmlOutputScope.OUTPUT_CLASS)) error("The element $element is not an output element")
        val endpoint = URL(resolveEndpoint(element))
        window.fetch("$endpoint/vision").then {response->
            TODO()
        }
    }

    public companion object {
        public const val OUTPUT_ENDPOINT_ATTRIBUTE: String = "data-output-endpoint"
        public const val DEFAULT_ENDPOINT: String = ".."
    }
}


