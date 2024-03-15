package space.kscience.visionforge.gdml.demo

import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Style
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.Document
import space.kscience.dataforge.context.Context
import space.kscience.gdml.GdmlShowCase
import space.kscience.visionforge.Colors
import space.kscience.visionforge.gdml.toVision
import space.kscience.visionforge.html.Application
import space.kscience.visionforge.html.VisionForgeStyles
import space.kscience.visionforge.html.startApplication
import space.kscience.visionforge.solid.ambientLight
import space.kscience.visionforge.solid.invoke
import space.kscience.visionforge.solid.three.ThreePlugin


private class GDMLDemoApp : Application {

    val context = Context("gdml-demo") {
        plugin(ThreePlugin)
    }

    override fun start(document: Document, state: Map<String, Any>) {

        val element = document.getElementById("application") ?: error("Element with id 'application' not found on page")

        val vision = GdmlShowCase.cubes().toVision().apply {
            ambientLight {
                color(Colors.white)
            }
        }

        renderComposable(element) {
            Style(VisionForgeStyles)
            Style {
                "html" {
                    height(100.percent)
                }

                "body" {
                    height(100.percent)
                    display(DisplayStyle.Flex)
                    alignItems(AlignItems.Stretch)
                }

                "#application" {
                    width(100.percent)
                    display(DisplayStyle.Flex)
                    alignItems(AlignItems.Stretch)
                }
            }
            GDMLApp(context, vision)
        }
    }
}

fun main() {
    startApplication(::GDMLDemoApp)
}