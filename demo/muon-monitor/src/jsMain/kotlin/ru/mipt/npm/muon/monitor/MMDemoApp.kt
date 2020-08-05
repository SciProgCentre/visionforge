package ru.mipt.npm.muon.monitor

import hep.dataforge.context.Global
import hep.dataforge.js.Application
import hep.dataforge.js.startApplication
import hep.dataforge.vision.spatial.SpatialVisionManager
import io.ktor.client.HttpClient
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import kotlinx.serialization.json.Json
import react.child
import react.dom.div
import react.dom.render
import kotlin.browser.document

private class MMDemoApp : Application {

    private val model = Model()

    private val connection = HttpClient {
        install(JsonFeature) {
            serializer = KotlinxSerializer(Json(context = SpatialVisionManager.serialModule))
        }
    }

    //TODO introduce react application

    override fun start(state: Map<String, Any>) {

        val context = Global.context("demo") {}
        val element = document.getElementById("app") ?: error("Element with id 'app' not found on page")

        render(element) {
            div("container-fluid h-100") {
                child(MMApp) {
                    attrs {
                        model = this@MMDemoApp.model
                        connection = this@MMDemoApp.connection
                        this.context = context
                    }
                }
            }
        }
    }
}

fun main() {
    startApplication(::MMDemoApp)
}