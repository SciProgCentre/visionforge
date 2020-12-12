package hep.dataforge.vision.three.server

import hep.dataforge.context.Context
import hep.dataforge.context.Global
import hep.dataforge.vision.client.VisionClient
import hep.dataforge.vision.client.renderAllVisions
import hep.dataforge.vision.solid.three.ThreePlugin
import kotlinx.browser.window

//FIXME check plugin loading in JS
//public actual val visionContext: Context = Global.context("vision-client") {
//    //Loading three-js renderer
//    plugin(ThreePlugin)
//}

public actual val visionContext: Context = Global.context("vision-client").apply {
    //Loading three-js renderer
    plugins.fetch(ThreePlugin)
}

public val clientManager: VisionClient get() = visionContext.plugins.fetch(VisionClient)


///**
// * Render all visions in the document using registered renderers
// */
//@JsExport
//public fun renderVisions() {
//    //Fetch from server and render visions for all outputs
//    window.onload = {
//        clientManager.renderAllVisions()
//    }
//}
//
///**
// * Render all visions in a given element, using registered renderers
// */
//@JsExport
//public fun renderAllVisionsAt(element: Element) {
//    clientManager.renderAllVisionsAt(element)
//}

public fun main() {
    //Fetch from server and render visions for all outputs
    window.onload = {
        clientManager.renderAllVisions()
    }
}