package space.kscience.visionforge.examples

import space.kscience.plotly.scatter
import space.kscience.visionforge.html.ResourceLocation
import space.kscience.visionforge.plotly.plotly

fun main() = makeVisionFile(resourceLocation = ResourceLocation.SYSTEM) {
    vision {
        plotly {
            scatter {
                x(1, 2, 3)
                y(5, 8, 7)
            }
        }
    }
}