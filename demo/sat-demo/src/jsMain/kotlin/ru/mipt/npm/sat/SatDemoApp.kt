package ru.mipt.npm.sat

import hep.dataforge.context.Global
import hep.dataforge.vision.client.VisionClient
import hep.dataforge.vision.client.fetchAndRenderAllVisions
import hep.dataforge.vision.solid.three.ThreePlugin
import kotlinx.browser.window

//private class SatDemoApp : Application {
//
//    override fun start(state: Map<String, Any>) {
//        val element = document.getElementById("canvas") as? HTMLElement
//            ?: error("Element with id 'canvas' not found on page")
//        val three = Global.plugins.fetch(ThreePlugin)
//
//        val sat = visionOfSatellite(
//            ySegments = 3,
//        )
//        three.render(element, sat){
//            minSize = 500
//            axes{
//                size = 500.0
//                visible = true
//            }
//        }
//    }
//
//}
//
//fun main() {
//    startApplication(::SatDemoApp)
//}

fun main() {
    //Loading three-js renderer
    Global.plugins.load(ThreePlugin)
    window.onload = {
        Global.plugins.fetch(VisionClient).fetchAndRenderAllVisions()
    }
}