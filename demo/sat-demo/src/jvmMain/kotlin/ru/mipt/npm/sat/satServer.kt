package ru.mipt.npm.sat


import hep.dataforge.context.Global
import hep.dataforge.names.asName
import hep.dataforge.vision.server.visionModule
import hep.dataforge.vision.solid.SolidManager
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.util.KtorExperimentalAPI
import kotlinx.html.script

@OptIn(KtorExperimentalAPI::class)
fun main() {
    val sat = visionOfSatellite(
        ySegments = 3,
    )

    val context = Global.context("SAT"){
        plugin(SolidManager)
    }

    embeddedServer(CIO, 8080, host = "localhost"){
        visionModule(context).apply {
            header {
                script {
                    src = "sat-demo.js"
                }
            }
            page {
                vision("main".asName(), sat)
            }
        }
    }.start(wait = true)
}