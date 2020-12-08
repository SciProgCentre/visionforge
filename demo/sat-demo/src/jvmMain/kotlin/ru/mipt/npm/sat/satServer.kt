package ru.mipt.npm.sat


import hep.dataforge.context.Global
import hep.dataforge.names.asName
import hep.dataforge.vision.VisionManager
import hep.dataforge.vision.get
import hep.dataforge.vision.server.close
import hep.dataforge.vision.server.serve
import hep.dataforge.vision.server.show
import hep.dataforge.vision.solid.Solid
import hep.dataforge.vision.solid.SolidManager
import hep.dataforge.vision.solid.color
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.html.h1
import kotlinx.html.script
import kotlin.random.Random

@OptIn(KtorExperimentalAPI::class)
fun main() {
    val sat = visionOfSatellite(
        ySegments = 3,
    )

    val context = Global.context("SAT") {
        plugin(SolidManager)
    }

    val server = context.plugins.fetch(VisionManager).serve {
        header {
            script {
                src = "sat-demo.js"
            }
        }
        page {
            h1 { +"Satellite detector demo" }
            vision("main".asName(), sat)
        }
        launch {
            while (isActive) {
                val currentLayer = Random.nextInt(10)
                (sat["layer[$currentLayer]"] as? Solid)?.color(123)
                delay(300)
                (sat["layer[$currentLayer]"] as? Solid)?.color = null
            }
        }
    }
    server.show()

    println("Press Enter to close server")
    while (readLine()!="exit"){
        //
    }

    server.close()

}