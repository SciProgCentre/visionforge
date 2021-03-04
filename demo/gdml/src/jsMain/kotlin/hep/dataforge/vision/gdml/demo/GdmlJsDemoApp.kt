package hep.dataforge.vision.gdml.demo

import hep.dataforge.context.Global
import hep.dataforge.vision.Application
import hep.dataforge.vision.bootstrap.useBootstrap
import hep.dataforge.vision.gdml.GdmlShowcase
import hep.dataforge.vision.gdml.toVision
import hep.dataforge.vision.solid.three.ThreePlugin
import hep.dataforge.vision.startApplication
import kotlinx.browser.document
import react.child
import react.dom.render


private class GDMLDemoApp : Application {

    override fun start(state: Map<String, Any>) {
        useBootstrap()

        val element = document.getElementById("app") ?: error("Element with id 'app' not found on page")

        val context = Global.context("demo") .apply{
            plugins.fetch(ThreePlugin)
        }
        render(element) {
            child(GDMLApp) {
                val vision = GdmlShowcase.cubes.toVision()
                //println(context.plugins.fetch(VisionManager).encodeToString(vision))
                attrs {
                    this.context = context
                    this.rootVision = vision
                }
            }
        }

    }
}

fun main() {
    startApplication(::GDMLDemoApp)
}