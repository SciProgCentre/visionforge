package space.kscience.visionforge.gdml.demo

import kotlinx.browser.document
import kotlinx.css.*
import react.dom.render
import space.kscience.dataforge.context.Context
import space.kscience.gdml.GdmlShowCase
import space.kscience.visionforge.Application
import space.kscience.visionforge.Colors
import space.kscience.visionforge.gdml.toVision
import space.kscience.visionforge.solid.ambientLight
import space.kscience.visionforge.solid.invoke
import space.kscience.visionforge.solid.three.ThreePlugin
import space.kscience.visionforge.startApplication
import styled.injectGlobal


private class GDMLDemoApp : Application {

    override fun start(state: Map<String, Any>) {
        val context = Context("gdml-demo"){
            plugin(ThreePlugin)
        }

        injectGlobal {
            html{
                height = 100.pct
            }

            body{
                height = 100.pct
                display = Display.flex
                alignItems = Align.stretch
            }

            "#application"{
                width = 100.pct
                display = Display.flex
                alignItems = Align.stretch
            }
        }

        val element = document.getElementById("application") ?: error("Element with id 'application' not found on page")

        render(element) {
            child(GDMLApp) {
                val vision = GdmlShowCase.cubes().toVision().apply {
                    ambientLight {
                        color(Colors.white)
                    }
                }
                //println(context.plugins.fetch(VisionManager).encodeToString(vision))
                attrs {
                    this.context = context
                    this.vision = vision
                }
            }
        }
    }
}

fun main() {
    startApplication(::GDMLDemoApp)
}