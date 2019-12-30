package ru.mipt.npm.muon.monitor.server


import hep.dataforge.vis.spatial.Visual3DPlugin
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.serialization.serialization
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import org.apache.commons.math3.random.JDKRandomGenerator
import ru.mipt.npm.muon.monitor.Model
import ru.mipt.npm.muon.monitor.sim.UniformTrackGenerator
import ru.mipt.npm.muon.monitor.sim.simulateOne
import java.io.File


fun Application.module() {
    val currentDir = File(".").absoluteFile
    environment.log.info("Current directory: $currentDir")

    val generator = UniformTrackGenerator(JDKRandomGenerator(223))
    install(DefaultHeaders)
    install(CallLogging)
    install(ContentNegotiation){
        serialization(json = Visual3DPlugin.json)
    }
    install(Routing) {
        get("/next") {
            call.respond(generator.simulateOne())
        }
        get("/geometry"){
            call.respond(Model.buildGeometry())
        }
        static("/") {
            resources()
        }
    }
}

fun main() {
    embeddedServer(CIO, 8080,host = "localhost", module = Application::module).start(wait = true)
}