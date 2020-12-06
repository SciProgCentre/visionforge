package ru.mipt.npm.sat


import hep.dataforge.context.Global
import hep.dataforge.names.asName
import hep.dataforge.vision.get
import hep.dataforge.vision.server.visionModule
import hep.dataforge.vision.solid.Solid
import hep.dataforge.vision.solid.SolidManager
import hep.dataforge.vision.solid.color
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.html.script
import kotlin.random.Random

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
        launch {
            while (isActive){
                val currentLayer = Random.nextInt(10)
                (sat["layer[$currentLayer]"] as? Solid)?.color(123)
                delay(300)
                (sat["layer[$currentLayer]"] as? Solid)?.color = null
            }
        }
    }.start(wait = true)
}