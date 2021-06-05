package ru.mipt.npm.muon.monitor

import io.ktor.client.HttpClient
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import kotlinx.browser.document
import react.child
import react.dom.render
import space.kscience.dataforge.context.Global
import space.kscience.dataforge.context.fetch
import space.kscience.visionforge.Application
import space.kscience.visionforge.VisionManager
import space.kscience.visionforge.bootstrap.useBootstrap
import space.kscience.visionforge.startApplication

private class MMDemoApp : Application {

    private val visionManager = Global.fetch(VisionManager)
    private val model = Model(visionManager)

    private val connection = HttpClient {
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }
    }

    override fun start(state: Map<String, Any>) {
        useBootstrap()

        val element = document.getElementById("app") ?: error("Element with id 'app' not found on page")

        val context = Global.buildContext("demo") {}
        render(element) {
            child(MMApp) {
                attrs {
                    this.model = this@MMDemoApp.model
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