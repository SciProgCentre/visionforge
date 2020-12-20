package ru.mipt.npm.sat


import hep.dataforge.names.toName
import hep.dataforge.vision.solid.Solid
import hep.dataforge.vision.solid.color
import hep.dataforge.vision.solid.invoke
import hep.dataforge.vision.three.server.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.html.div
import kotlinx.html.h1
import kotlin.random.Random

fun main() {
    //Create a geometry
    val sat = visionOfSatellite(ySegments = 3)

    val server = visionManager.serve {
        //use client library
        useThreeJs()
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

    GlobalScope.launch {
        while (isActive) {
            val randomLayer = Random.nextInt(1, 11)
            val randomI = Random.nextInt(1, 4)
            val randomJ = Random.nextInt(1, 4)
            val target = "layer[$randomLayer].segment[$randomI,$randomJ]".toName()
            val targetVision = sat[target] as Solid
            targetVision.color("red")
            delay(300)
            targetVision.color("darkgreen")
            delay(10)
        }
    }

    println("Enter 'exit' to close server")
    while (readLine() != "exit") {
        //
    }

    server.close()

}