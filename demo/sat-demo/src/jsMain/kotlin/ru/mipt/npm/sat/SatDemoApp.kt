package ru.mipt.npm.sat

import hep.dataforge.Application
import hep.dataforge.context.Global
import hep.dataforge.meta.invoke
import hep.dataforge.startApplication
import hep.dataforge.vision.solid.three.ThreePlugin
import hep.dataforge.vision.solid.three.render
import kotlinx.browser.document
import org.w3c.dom.HTMLElement

private class SatDemoApp : Application {

    override fun start(state: Map<String, Any>) {
        val element = document.getElementById("canvas") as? HTMLElement
            ?: error("Element with id 'canvas' not found on page")
        val three = Global.plugins.fetch(ThreePlugin)
        val sat = visionOfSatellite(
            ySegments = 3,
        )
        three.render(element, sat){
            minSize = 500
            axes{
                size = 500.0
                visible = true
            }
        }
    }

}

fun main() {
    startApplication(::SatDemoApp)
}