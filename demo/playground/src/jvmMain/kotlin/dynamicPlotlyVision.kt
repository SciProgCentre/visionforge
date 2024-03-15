package space.kscience.visionforge.examples

import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.staticResources
import io.ktor.server.routing.routing
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.html.a
import kotlinx.html.h1
import space.kscience.dataforge.context.Global
import space.kscience.dataforge.context.request
import space.kscience.plotly.layout
import space.kscience.plotly.models.Trace
import space.kscience.plotly.models.invoke
import space.kscience.visionforge.html.VisionPage
import space.kscience.visionforge.plotly.PlotlyPlugin
import space.kscience.visionforge.plotly.plotly
import space.kscience.visionforge.server.close
import space.kscience.visionforge.server.openInBrowser
import space.kscience.visionforge.server.visionPage
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

fun main() {
    val plotlyPlugin = Global.request(PlotlyPlugin)
    val visionManager = plotlyPlugin.visionManager

    val server = embeddedServer(CIO) {

        routing {
            staticResources("/", null)
        }


        val freq = 1.0 / 1000
        val oscillationFreq = 1.0 / 10000

        val x = (0..100).map { it.toDouble() / 100.0 }
        val sinY = x.map { sin(2.0 * PI * it) }
        val cosY = x.map { cos(2.0 * PI * it) }

        val sinTrace = Trace(x, sinY) { name = "sin" }
        val cosTrace = Trace(x, cosY) { name = "cos" }

        visionPage(
            visionManager,
            VisionPage.scriptHeader("js/visionforge-playground.js"),
        ) {

            h1 { +"This is the plot page" }
            a("/other") { +"The other page" }
            vision {


                plotly {
                    traces(sinTrace, cosTrace)
                    layout {
                        title = "Other dynamic plot"
                        xaxis.title = "x axis name"
                        yaxis.title = "y axis name"
                    }
                }
            }
        }

        visionPage(
            visionManager,
            VisionPage.scriptHeader("js/visionforge-playground.js"),
            route = "other"
        ) {
            h1 { +"This is the other plot page" }
            a("/") { +"Back to the main page" }
            vision {
                plotly {
                    traces(sinTrace)
                    layout {
                        title = "Dynamic plot"
                        xaxis.title = "x axis name"
                        yaxis.title = "y axis name"
                    }
                }
            }
        }


        //Start pushing updates
        launch {
            var time: Long = 0

            while (isActive) {
                delay(10)
                time += 10
                sinTrace.y.numbers = x.map { sin(2.0 * PI * (it + time.toDouble() * freq)) }
                val cosAmp = cos(2.0 * PI * oscillationFreq * time)
                cosTrace.y.numbers = x.map { cos(2.0 * PI * (it + time.toDouble() * freq)) * cosAmp }
            }
        }

    }.start(false)


    server.openInBrowser()

    while (readlnOrNull() != "exit") {

    }

    server.close()
}
