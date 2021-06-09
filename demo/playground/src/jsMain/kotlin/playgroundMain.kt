import space.kscience.dataforge.misc.DFExperimental
import space.kscience.visionforge.plotly.PlotlyPlugin
import space.kscience.visionforge.ring.ThreeWithControlsPlugin
import space.kscience.visionforge.runVisionClient

@DFExperimental
fun main() = runVisionClient {
    plugin(PlotlyPlugin)
    plugin(ThreeWithControlsPlugin)
}