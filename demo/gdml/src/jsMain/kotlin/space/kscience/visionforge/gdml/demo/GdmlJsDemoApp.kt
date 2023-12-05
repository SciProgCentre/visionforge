package space.kscience.visionforge.gdml.demo

import kotlinx.css.*
import org.w3c.dom.Document
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.request
import space.kscience.gdml.GdmlShowCase
import space.kscience.visionforge.Application
import space.kscience.visionforge.Colors
import space.kscience.visionforge.gdml.toVision
import space.kscience.visionforge.react.createRoot
import space.kscience.visionforge.react.render
import space.kscience.visionforge.solid.Solids
import space.kscience.visionforge.solid.ambientLight
import space.kscience.visionforge.solid.invoke
import space.kscience.visionforge.solid.three.ThreePlugin
import space.kscience.visionforge.startApplication
import styled.injectGlobal


private class GDMLDemoApp : Application {

    override fun start(document: Document, state: Map<String, Any>) {
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

        createRoot(element).render {
            child(GDMLApp) {
                val vision = GdmlShowCase.cubes().toVision().apply {
                    ambientLight {
                        color(Colors.white)
                    }
                }
                //println(context.plugins.fetch(VisionManager).encodeToString(vision))
                attrs {
                    this.solids = context.request(Solids)
                    this.vision = vision
                }
            }
        }
    }
}

fun main() {
    startApplication(::GDMLDemoApp)
}