package misc

import kotlinx.coroutines.*
import space.kscience.plotly.Plotly
import space.kscience.plotly.Plotly.plot
import space.kscience.plotly.layout
import space.kscience.plotly.models.histogram
import space.kscience.visionforge.plotly.serveSinglePage
import space.kscience.visionforge.server.openInBrowser
import kotlin.random.Random

@OptIn(DelicateCoroutinesApi::class)
suspend fun main() {
    val server = Plotly.serveSinglePage {
        val rnd = Random(222)
        plot {
            histogram {
                name = "Random data"
                GlobalScope.launch {
                    while (isActive) {
                        x.numbers = List(500) { rnd.nextDouble() }
                        delay(300)
                    }
                }
            }

            layout {
                bargap = 0.1
                title {
                    text = "Basic Histogram"
                    font {
                        size = 20
                        color("black")
                    }
                }
                xaxis {
                    title {
                        text = "Value"
                        font {
                            size = 16
                        }
                    }
                }
                yaxis {
                    title {
                        text = "Count"
                        font {
                            size = 16
                        }
                    }
                }
            }
        }
    }

    server.openInBrowser()

    println("Press Enter to close server")
    readlnOrNull()

    server.stop()

}