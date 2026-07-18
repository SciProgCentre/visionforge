import kotlinx.coroutines.*
import space.kscience.plotly.Plotly
import space.kscience.plotly.layout
import space.kscience.plotly.models.Bar
import space.kscience.plotly.models.invoke
import space.kscience.plotly.plot
import space.kscience.visionforge.plotly.serveSinglePage
import space.kscience.visionforge.server.openInBrowser
import kotlin.random.Random


@OptIn(DelicateCoroutinesApi::class)
suspend fun main() {
    val initialValue = (1..10).toList()

    val traces = (0..2).associate { i ->
        val name = "Series $i"

        name to Bar {
            x.strings = initialValue.map { "Column: $it" }
            y.numbers = initialValue
            text.strings = initialValue.map { "Initial value of this datapoint is: ${it}" }
            this.name = name
        }
    }

    val server = Plotly.serveSinglePage ( port = 3872) {
        //root level plots go to default page
        plot {
            traces(traces.values)
            layout {
                title = "Other dynamic plot"
                xaxis.title = "x axis name"
                yaxis.title = "y axis name"
            }
        }
    }

    server.openInBrowser()

    //Start pushing updates
    GlobalScope.launch {
        delay(1000)
        while (isActive) {
            repeat(10) { columnIndex ->
                repeat(3) { seriesIndex ->
                    delay(200)
                    traces["Series $seriesIndex"]?.let { bar ->
                        println("Updating ${bar.name}, Column $columnIndex")
                        //TODO replace with dynamic data API
                        val yValues = bar.y.doubles
                        yValues[columnIndex] = Random.nextInt(0, 100).toDouble()
                        bar.y.doubles = yValues
                        bar.text.strings = yValues.map { "Updated value of this datapoint is: $it" }
                    }
                }
            }
        }
    }

    println("Press Enter to close server")
    readlnOrNull()

    server.stop()
}
