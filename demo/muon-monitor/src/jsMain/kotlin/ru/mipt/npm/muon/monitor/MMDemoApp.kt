package ru.mipt.npm.muon.monitor

import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.Document
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.request
import space.kscience.visionforge.Application
import space.kscience.visionforge.VisionManager
import space.kscience.visionforge.solid.Solids
import space.kscience.visionforge.solid.three.ThreePlugin
import space.kscience.visionforge.startApplication

private class MMDemoApp : Application {

    override fun start(document: Document, state: Map<String, Any>) {

        val context = Context("MM-demo") {
            plugin(ThreePlugin)
        }

        val visionManager = context.request(VisionManager)

        val model = Model(visionManager)

        val element = document.getElementById("app") ?: error("Element with id 'app' not found on page")
        renderComposable(element) {
            MMApp(context.request(Solids), model)
        }
    }
}

fun main() {
    startApplication(::MMDemoApp)
}