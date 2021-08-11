package ru.mipt.npm.muon.monitor

import io.ktor.client.HttpClient
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import kotlinx.browser.document
import react.child
import react.dom.render
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.fetch
import space.kscience.visionforge.Application
import space.kscience.visionforge.VisionManager
import space.kscience.visionforge.bootstrap.useBootstrap
import space.kscience.visionforge.solid.three.ThreePlugin
import space.kscience.visionforge.startApplication

private class MMDemoApp : Application {

    private val connection = HttpClient {
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }
    }

    override fun start(state: Map<String, Any>) {
        useBootstrap()

        val context = Context("MM-demo"){
            plugin(ThreePlugin)
        }
        val visionManager = context.fetch(VisionManager)

        val model = Model(visionManager)

        val element = document.getElementById("app") ?: error("Element with id 'app' not found on page")
        render(element) {
            child(MMApp) {
                attrs {
                    this.model = model
                    this.connection = this@MMDemoApp.connection
                    this.context = context
                }
            }
        }
    }
}

fun main() {
    startApplication(::MMDemoApp)
}