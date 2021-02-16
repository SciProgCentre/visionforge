package hep.dataforge.vision.plotly

import hep.dataforge.meta.Config
import hep.dataforge.misc.DFExperimental
import hep.dataforge.vision.VisionBase
import hep.dataforge.vision.html.VisionOutput
import kotlinx.serialization.Serializable
import kscience.plotly.Plot
import kscience.plotly.Plotly

@Serializable
public class VisionOfPlotly(private val plotConfig: Config) : VisionBase(plotConfig){
    public val plot: Plot get() = Plot(plotConfig)
}

@DFExperimental
public inline fun VisionOutput.plotly(block: Plot.() -> Unit): VisionOfPlotly = VisionOfPlotly(Plotly.plot(block).config)