package hep.dataforge.vision.three.server

import hep.dataforge.context.Global
import hep.dataforge.vision.client.VisionClient
import hep.dataforge.vision.client.renderAllVisions
import hep.dataforge.vision.solid.three.ThreePlugin
import kotlinx.browser.window

public fun main() {
    //Loading three-js renderer
    val visionContext = Global.context("threejs") {
        plugin(ThreePlugin)
    }
    val clientManager = visionContext.plugins.fetch(VisionClient)

    //Fetch from server and render visions for all outputs
    window.onload = {
        clientManager.renderAllVisions()
    }
}