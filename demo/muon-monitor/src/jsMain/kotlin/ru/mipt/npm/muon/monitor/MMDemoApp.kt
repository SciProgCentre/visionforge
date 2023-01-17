package ru.mipt.npm.muon.monitor

import org.w3c.dom.Document
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.fetch
import space.kscience.visionforge.Application
import space.kscience.visionforge.VisionManager
import space.kscience.visionforge.react.createRoot
import space.kscience.visionforge.react.render
import space.kscience.visionforge.solid.Solids
import space.kscience.visionforge.solid.three.ThreePlugin
import space.kscience.visionforge.startApplication

private class MMDemoApp : Application {

    override fun start(document: Document, state: Map<String, Any>) {

        val context = Context("MM-demo") {
            plugin(ThreePlugin)
        }

        val visionManager = context.fetch(VisionManager)

        val model = Model(visionManager)

        val element = document.getElementById("app") ?: error("Element with id 'app' not found on page")
        createRoot(element).render {
            child(MMApp) {
                attrs {
                    this.model = model
                    this.solids = context.fetch(Solids)
                }
            }
        }
    }
}

fun main() {
    startApplication(::MMDemoApp)
}