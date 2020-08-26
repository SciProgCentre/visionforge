package hep.dataforge.vision.gdml.demo

import hep.dataforge.context.Global
import hep.dataforge.js.Application
import hep.dataforge.js.startApplication
import hep.dataforge.vision.gdml.GDMLTransformer
import hep.dataforge.vision.gdml.LUnit
import hep.dataforge.vision.gdml.toVision
import hep.dataforge.vision.solid.SolidMaterial.Companion.MATERIAL_OPACITY_KEY
import kotlinx.css.*
import react.child
import react.dom.render
import styled.injectGlobal
import kotlin.browser.document


private class GDMLDemoApp : Application {

    override fun start(state: Map<String, Any>) {

        injectGlobal {
            body {
                height = 100.pct
                width = 100.pct
                margin(0.px)
                padding(0.px)
            }
        }

        val context = Global.context("demo") {}
        val element = document.getElementById("app") ?: error("Element with id 'app' not found on page")

        render(element) {
            child(GDMLApp) {
                attrs {
                    this.context = context
                    this.rootObject = cubes().toVision()
                }
            }
        }

    }
}

fun main() {
    startApplication(::GDMLDemoApp)
}