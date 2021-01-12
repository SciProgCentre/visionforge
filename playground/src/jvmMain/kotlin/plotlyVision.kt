package hep.dataforge.vision.examples

import hep.dataforge.meta.DFExperimental
import hep.dataforge.vision.VisionForge
import hep.dataforge.vision.VisionManager
import hep.dataforge.vision.html.fragment
import hep.dataforge.vision.plotly.plotly
import hep.dataforge.vision.plotly.withPlotly
import kscience.plotly.scatter

@DFExperimental
fun main() {
    val fragment = VisionManager.fragment {
        vision {
            plotly {
                scatter {
                    x(1,2,3)
                    y(5,8,7)
                }
            }
        }
    }

    VisionForge.withPlotly().makeVisionFile(fragment)
}