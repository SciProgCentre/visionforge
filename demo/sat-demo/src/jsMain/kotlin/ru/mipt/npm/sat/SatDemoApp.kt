package ru.mipt.npm.sat

import hep.dataforge.context.Global
import hep.dataforge.vision.client.VisionClient
import hep.dataforge.vision.client.fetchAndRenderAllVisions
import hep.dataforge.vision.solid.three.ThreePlugin
import kotlinx.browser.window

fun main() {
    //Loading three-js renderer
    Global.plugins.load(ThreePlugin)
    //Fetch from server and render visions for all outputs
    window.onload = {
        Global.plugins.fetch(VisionClient).fetchAndRenderAllVisions()
    }
}