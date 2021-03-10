package space.kscience.visionforge.gdml.demo

import kotlinx.browser.document
import react.child
import react.dom.render
import space.kscience.dataforge.context.Global
import space.kscience.visionforge.Application
import space.kscience.visionforge.bootstrap.useBootstrap
import space.kscience.visionforge.gdml.GdmlShowcase
import space.kscience.visionforge.gdml.toVision
import space.kscience.visionforge.solid.three.ThreePlugin
import space.kscience.visionforge.startApplication


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