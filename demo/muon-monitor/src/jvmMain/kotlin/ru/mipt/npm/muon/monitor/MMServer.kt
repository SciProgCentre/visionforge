package ru.mipt.npm.muon.monitor.server


import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.resources
import io.ktor.server.http.content.static
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import org.apache.commons.math3.random.JDKRandomGenerator
import ru.mipt.npm.muon.monitor.Model
import ru.mipt.npm.muon.monitor.sim.Cos2TrackGenerator
import ru.mipt.npm.muon.monitor.sim.simulateOne
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.Global
import space.kscience.dataforge.context.request
import space.kscience.dataforge.misc.DFExperimental
import space.kscience.visionforge.solid.Solids
import java.awt.Desktop
import java.io.File
import java.net.URI

private val generator = Cos2TrackGenerator(JDKRandomGenerator(223))

@OptIn(DFExperimental::class)
fun Application.module(context: Context = Global) {
    val currentDir = File(".").absoluteFile
    environment.log.info("Current directory: $currentDir")
    val solidManager = context.request(Solids)

    install(ContentNegotiation) {
        json()
    }
    install(Routing) {
        get("/event") {
            val event = generator.simulateOne()
            call.respond(event)
        }
        get("/geometry") {
            call.respondText(
                Model(solidManager.visionManager).encodeToString(),
                contentType = ContentType.Application.Json,
                status = HttpStatusCode.OK
            )
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