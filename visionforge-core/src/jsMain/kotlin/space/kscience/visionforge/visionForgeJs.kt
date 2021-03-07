package space.kscience.visionforge

import kotlinx.browser.document
import kotlinx.browser.window
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.misc.DFExperimental

@JsExport
@DFExperimental
public actual object VisionForge {
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

private val visionForgeContext = Context("VisionForge") {
    plugin(VisionClient)
}

@DFExperimental
public actual val VisionForge.context: Context
    get() = visionForgeContext

@DFExperimental
public val VisionForge.visionClient: VisionClient
    get() = plugins.fetch(VisionClient)


///**
// * Render all visions in this [window] using current global state of [VisionForge]
// */
//@DFExperimental
//@JsExport
//public fun renderVisionsInWindow(): Unit {
//    VisionForge.renderVisionsInWindow()
//}
//
///**
// * Render all visions in an element with a given [id]
// */
//@DFExperimental
//@JsExport
//public fun renderVisionsAt(id: String): Unit {
//    VisionForge.renderVisionsAt(id)
//}