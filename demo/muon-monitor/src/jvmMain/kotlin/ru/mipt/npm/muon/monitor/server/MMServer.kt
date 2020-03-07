package ru.mipt.npm.muon.monitor.server


import hep.dataforge.vis.spatial.Visual3D
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.application.log
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
import ru.mipt.npm.muon.monitor.sim.Cos2TrackGenerator
import ru.mipt.npm.muon.monitor.sim.simulateOne
import java.awt.Desktop
import java.io.File
import java.net.URI

private val generator = Cos2TrackGenerator(JDKRandomGenerator(223))

fun Application.module() {
    val currentDir = File(".").absoluteFile
    environment.log.info("Current directory: $currentDir")


    install(DefaultHeaders)
    install(CallLogging)
    install(ContentNegotiation) {
        serialization(json = Visual3D.json)
    }
    install(Routing) {
        get("/event") {
            val event = generator.simulateOne()
            call.respond(event)
        }
        get("/geometry") {
            call.respond(Model.buildGeometry())
        }
        static("/") {
            resources()
        }
    }
    try {
        Desktop.getDesktop().browse(URI("http://localhost:8080/index.html"))
    } catch (ex: Exception) {
        log.error("Failed to launch browser", ex)
    }
}

fun main() {
    embeddedServer(CIO, 8080, host = "localhost", module = Application::module).start(wait = true)
}