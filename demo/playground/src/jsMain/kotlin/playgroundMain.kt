import space.kscience.dataforge.misc.DFExperimental
import space.kscience.visionforge.plotly.PlotlyPlugin
import space.kscience.visionforge.runVisionClient
import space.kscience.visionforge.solid.three.ThreePlugin

@DFExperimental
fun main() = runVisionClient {
    plugin(PlotlyPlugin)
    plugin(ThreePlugin)
}