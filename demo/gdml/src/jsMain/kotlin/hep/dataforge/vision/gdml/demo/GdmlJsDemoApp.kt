package hep.dataforge.vision.gdml.demo

import hep.dataforge.context.Global
import hep.dataforge.vision.Application
import hep.dataforge.vision.gdml.toVision
import hep.dataforge.vision.startApplication
import kotlinx.browser.document
import react.child
import react.dom.render


private class GDMLDemoApp : Application {

    override fun start(state: Map<String, Any>) {
        val element = document.getElementById("app") ?: error("Element with id 'app' not found on page")

        val context = Global.context("demo") {}
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