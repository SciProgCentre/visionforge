import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import kotlinx.coroutines.*
import kotlinx.html.a
import kotlinx.html.h1
import space.kscience.plotly.layout
import space.kscience.plotly.models.Trace
import space.kscience.plotly.models.invoke
import space.kscience.plotly.plot
import space.kscience.visionforge.plotly.plotlyPage
import space.kscience.visionforge.server.openInBrowser
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin


@OptIn(DelicateCoroutinesApi::class)
suspend fun main() {

    val freq = 1.0 / 1000
    val oscillationFreq = 1.0 / 10000

    val x = (0..100).map { it.toDouble() / 100.0 }
    val sinY = x.map { sin(2.0 * PI * it) }
    val cosY = x.map { cos(2.0 * PI * it) }

    val sinTrace = Trace(x, sinY) { name = "sin" }
    val cosTrace = Trace(x, cosY) { name = "cos" }

    val server = embeddedServer(CIO, port = 7878) {

        //root level plots go to default page
        plotlyPage {
            h1 { +"This is the plot page" }
            a("/other") { +"The other page" }
            plot {
                traces(sinTrace, cosTrace)
                layout {
                    title = "Other dynamic plot"
                    xaxis.title = "x axis name"
                    yaxis.title = "y axis name"
                }
            }
        }

        plotlyPage("other") {
            h1 { +"This is the other plot page" }
            a("/") { +"Back to the main page" }
            plot {
                traces(sinTrace)
                layout {
                    title = "Dynamic plot"
                    xaxis.title = "x axis name"
                    yaxis.title = "y axis name"
                }
            }
        }
    }.start(false)

    server.openInBrowser()

    //Start pushing updates
    GlobalScope.launch {
        var time: Long = 0

        while (isActive) {
            delay(10)
            time += 10
            sinTrace.y.numbers = x.map { sin(2.0 * PI * (it + time.toDouble() * freq)) }
            val cosAmp = cos(2.0 * PI * oscillationFreq * time)
            cosTrace.y.numbers = x.map { cos(2.0 * PI * (it + time.toDouble() * freq)) * cosAmp }
        }
    }

    println("Press Enter to close server")
    while (readlnOrNull()?.trim() != "exit") {
        //wait
    }

    server.stop()
}