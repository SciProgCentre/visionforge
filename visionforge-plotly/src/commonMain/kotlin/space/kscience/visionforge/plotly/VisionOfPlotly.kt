package space.kscience.visionforge.plotly

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.dataforge.misc.DFExperimental
import space.kscience.plotly.Plot
import space.kscience.plotly.Plotly
import space.kscience.visionforge.VisionBase
import space.kscience.visionforge.html.VisionOutput

@Serializable
@SerialName("vision.plotly")
public class VisionOfPlotly private constructor() : VisionBase() {

    public constructor(plot: Plot) : this() {
        properties = plot.meta
    }

    public val plot: Plot get() = Plot(meta)
}

public fun Plot.asVision(): VisionOfPlotly = VisionOfPlotly(this)

@DFExperimental
public inline fun VisionOutput.plotly(
    block: Plot.() -> Unit,
): VisionOfPlotly {
    requirePlugin(PlotlyPlugin)
    return VisionOfPlotly(Plotly.plot(block))
}