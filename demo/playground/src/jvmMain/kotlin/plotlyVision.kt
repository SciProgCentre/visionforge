package hep.dataforge.vision.examples

import hep.dataforge.misc.DFExperimental
import hep.dataforge.vision.VisionForge
import hep.dataforge.vision.html.ResourceLocation
import hep.dataforge.vision.html.fragment
import hep.dataforge.vision.invoke
import hep.dataforge.vision.plotly.plotly
import hep.dataforge.vision.plotly.usePlotly
import kscience.plotly.scatter

@DFExperimental
fun main() = VisionForge {
    val fragment = fragment {
        vision {
            plotly {
                scatter {
                    x(1, 2, 3)
                    y(5, 8, 7)
                }
            }
        }
    }
    usePlotly()
    makeVisionFile(fragment, resourceLocation = ResourceLocation.SYSTEM)
}