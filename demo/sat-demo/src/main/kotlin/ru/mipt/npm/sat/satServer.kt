package ru.mipt.npm.sat


import kotlinx.coroutines.*
import kotlinx.html.div
import kotlinx.html.h1
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.names.Name
import space.kscience.visionforge.html.Page
import space.kscience.visionforge.html.plus
import space.kscience.visionforge.server.close
import space.kscience.visionforge.server.openInBrowser
import space.kscience.visionforge.server.serve
import space.kscience.visionforge.solid.*
import space.kscience.visionforge.three.threeJsHeader
import space.kscience.visionforge.visionManager
import kotlin.random.Random


fun main() {
    val satContext = Context("sat") {
        plugin(Solids)
    }

    //Create a geometry
    val sat = visionOfSatellite(ySegments = 3)

    val server = satContext.visionManager.serve {
        page(header = Page.threeJsHeader + Page.styleSheetHeader("css/styles.css")) {
            div("flex-column") {
                h1 { +"Satellite detector demo" }
                vision { sat }
            }
        }
    }

    server.openInBrowser()

    @OptIn(DelicateCoroutinesApi::class)
    GlobalScope.launch {
        while (isActive) {
            val randomLayer = Random.nextInt(1, 11)
            val randomI = Random.nextInt(1, 4)
            val randomJ = Random.nextInt(1, 4)
            val target = Name.parse("layer[$randomLayer].segment[$randomI,$randomJ]")
            val targetVision = sat[target] as Solid
            targetVision.color.set("red")
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