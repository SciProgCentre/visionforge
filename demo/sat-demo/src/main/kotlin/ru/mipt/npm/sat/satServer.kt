package ru.mipt.npm.sat


import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.html.div
import kotlinx.html.h1
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.names.Name
import space.kscience.visionforge.solid.*
import space.kscience.visionforge.three.server.*
import space.kscience.visionforge.visionManager
import kotlin.random.Random

fun main() {
    val satContext = Context("sat") {
        plugin(Solids)
    }

    //Create a geometry
    val sat = visionOfSatellite(ySegments = 3)

    val server = satContext.visionManager.serve {
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

    server.openInBrowser()

    GlobalScope.launch {
        while (isActive) {
            val randomLayer = Random.nextInt(1, 11)
            val randomI = Random.nextInt(1, 4)
            val randomJ = Random.nextInt(1, 4)
            val target = Name.parse("layer[$randomLayer].segment[$randomI,$randomJ]")
            val targetVision = sat[target] as Solid
            targetVision.color("red")
            delay(1000)
            targetVision.color.clear()
            delay(500)
        }
    }

    println("Enter 'exit' to close server")
    while (readLine() != "exit") {
        //
    }

    server.close()

}