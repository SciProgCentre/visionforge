package ru.mipt.npm.muon.monitor

import hep.dataforge.context.Global
import hep.dataforge.vision.Application
import hep.dataforge.vision.bootstrap.useBootstrap
import hep.dataforge.vision.startApplication
import io.ktor.client.HttpClient
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import kotlinx.browser.document
import react.child
import react.dom.render

private class MMDemoApp : Application {

    private val model = Model()

    private val connection = HttpClient {
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }
    }

    override fun start(state: Map<String, Any>) {
        useBootstrap()

        val element = document.getElementById("app") ?: error("Element with id 'app' not found on page")

        val context = Global.context("demo") {}
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