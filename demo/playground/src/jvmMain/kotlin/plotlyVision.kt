package space.kscience.visionforge.examples

import space.kscience.dataforge.misc.DFExperimental
import space.kscience.plotly.scatter
import space.kscience.visionforge.VisionForge
import space.kscience.visionforge.html.ResourceLocation
import space.kscience.visionforge.html.fragment
import space.kscience.visionforge.invoke
import space.kscience.visionforge.plotly.PlotlyPlugin
import space.kscience.visionforge.plotly.plotly

@DFExperimental
fun main() = VisionForge(PlotlyPlugin) {
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
    makeVisionFile(fragment, resourceLocation = ResourceLocation.SYSTEM)
}