package hep.dataforge.vision

import hep.dataforge.context.Context
import kotlinx.browser.document
import kotlinx.browser.window

@JsExport
public actual object VisionForge{
    /**
     * Render all visions in this [window] using current global state of [VisionForge]
     */
    public fun renderVisionsInWindow() {
        window.onload = {
            visionClient.renderAllVisions()
        }
    }

    /**
     * Render all visions in an element with a given [id]
     */
    public fun renderVisionsAt(id: String) {
        val element = document.getElementById(id)
        if (element != null) {
            visionClient.renderAllVisionsAt(element)
        } else {
            console.warn("Element with id $id not found")
        }
    }
}

private val visionForgeContext = Context("VisionForge"){
    plugin(VisionClient)
}

public actual val VisionForge.context: Context get() = visionForgeContext

public val VisionForge.visionClient: VisionClient get() = plugins.fetch(VisionClient)