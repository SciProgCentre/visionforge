package candlestick

import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import space.kscience.plotly.Plotly.plot
import space.kscience.plotly.layout
import space.kscience.plotly.models.AxisType
import space.kscience.plotly.models.DragMode
import space.kscience.visionforge.plotly.plotlyPage
import space.kscience.visionforge.server.openInBrowser
import kotlin.random.Random

suspend fun main() {
    val server = embeddedServer(CIO, 7777) {
        plotlyPage {
            plot {
                traces(candleStickTrace)
                layout {
                    dragmode = DragMode.zoom
                    margin {
                        r = 10
                        t = 25
                        b = 40
                        l = 60
                    }
                    showlegend = false
                    xaxis {
                        type = AxisType.date
                        range("2017-01-03 12:00".."2017-02-15 12:00")
                        //rangeslider: { range: ["2017-01-03 12:00", "2017-02-15 12:00"] },
                        title = "Date"
                    }
                    yaxis {
                        range(114.609999778..137.410004222)
                    }
                }

                launch {
                    while (isActive) {
                        delay(400)
                        candleStickTrace.open.numbers =
                            candleStickTrace.open.doubles.map { it + Random.nextDouble() - 0.5 }
                    }
                }
            }
        }
    }
    server.openInBrowser()
    println("Enter 'exit' to close server")
    while (readlnOrNull()?.trim() != "exit") {
        //wait
    }

    server.stop()
}