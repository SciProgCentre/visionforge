package space.kscience.visionforge.plotly

import space.kscience.plotly.Plotly
import space.kscience.plotly.scatter
import kotlin.test.Test
import kotlin.test.assertTrue

class VisionOfPlotlyTest {
    @Test
    fun conversion(){
        val plot = Plotly.plot {
            scatter {
                x(1,2,3)
                y(1,2,3)
            }
        }
        val vision = VisionOfPlotly(plot)
//        println(vision.plot.toJsonString())
//        println(vision.plot.data.toJsonString())
        assertTrue { vision.plot.data.first().x.doubles.size == 3}
    }
}