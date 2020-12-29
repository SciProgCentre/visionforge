package hep.dataforge.vision.plotly

import hep.dataforge.meta.DFExperimental
import hep.dataforge.vision.VisionBase
import hep.dataforge.vision.html.VisionOutput
import kscience.plotly.Plot
import kscience.plotly.Plotly

public class VisionOfPlotly(public val plot: Plot): VisionBase(plot.config)

@DFExperimental
public inline fun VisionOutput.plotly(block: Plot.() -> Unit): VisionOfPlotly = VisionOfPlotly(Plotly.plot(block))