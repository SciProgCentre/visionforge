package ru.mipt.npm.sat


import hep.dataforge.context.Global
import hep.dataforge.names.toName
import hep.dataforge.vision.server.*
import hep.dataforge.vision.solid.Solid
import hep.dataforge.vision.solid.SolidManager
import hep.dataforge.vision.solid.color
import hep.dataforge.vision.visionManager
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.html.div
import kotlinx.html.h1
import kotlin.random.Random

@OptIn(KtorExperimentalAPI::class)
fun main() {
    //Create a geometry
    val sat = visionOfSatellite(ySegments = 3)

    val context = Global.context("SAT") {
        //need to install solids extension, vision manager is installed automatically
        plugin(SolidManager)
    }

    // fetch vision manager
    val visionManager = context.visionManager

    val server = visionManager.serve {
        //use client library
        useScript("visionforge-solid.js")
        //use css
        useCss("css/styles.css")
        page {
            div("flex-column") {
                h1 { +"Satellite detector demo" }
                vision(sat)
            }
        }
    }

    server.show()

    context.launch {
        while (isActive) {
            val target = "layer[${Random.nextInt(1, 11)}].segment[${Random.nextInt(3)},${Random.nextInt(3)}]".toName()
            (sat[target] as? Solid)?.color("red")
            delay(300)
            (sat[target] as? Solid)?.color = "green"
            delay(10)
        }
    }

    println("Press Enter to close server")
    while (readLine() != "exit") {
        //
    }

    server.close()

}