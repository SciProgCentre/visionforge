package space.kscience.visionforge.plotly

import kotlinx.serialization.Serializable
import space.kscience.dataforge.meta.Config
import space.kscience.dataforge.misc.DFExperimental
import space.kscience.plotly.Plot
import space.kscience.plotly.Plotly
import space.kscience.visionforge.VisionBase
import space.kscience.visionforge.html.VisionOutput

@Serializable
public class VisionOfPlotly(private val plotConfig: Config) : VisionBase(plotConfig){
    public val plot: Plot get() = Plot(plotConfig)
}

public fun Plot.toVision(): VisionOfPlotly = VisionOfPlotly(config)

@DFExperimental
public inline fun VisionOutput.plotly(block: Plot.() -> Unit): VisionOfPlotly = VisionOfPlotly(Plotly.plot(block).config)