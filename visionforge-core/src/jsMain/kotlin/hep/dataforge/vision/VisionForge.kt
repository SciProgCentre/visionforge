package hep.dataforge.vision

import hep.dataforge.context.Context
import hep.dataforge.context.Global
import hep.dataforge.vision.client.VisionClient
import hep.dataforge.vision.client.renderAllVisions
import hep.dataforge.vision.client.renderAllVisionsAt
import kotlinx.browser.document
import kotlinx.browser.window

public actual val VisionForge: Context = Global.context("VisionForge").apply {
    plugins.fetch(VisionManager)
    plugins.fetch(VisionClient)
}


/**
 * Render all visions in this [window] using current global state of [VisionForge]
 */
@JsExport
public fun renderVisionsInWindow() {
    window.onload = {
        VisionForge.plugins[VisionClient]?.renderAllVisions()
    }
}

@JsExport
public fun renderVisionsAt(id: String) {
    val element = document.getElementById(id)
    if (element != null) {
        VisionForge.plugins[VisionClient]?.renderAllVisionsAt(element)
    } else {
        console.warn("Element with id $id not found")
    }
}