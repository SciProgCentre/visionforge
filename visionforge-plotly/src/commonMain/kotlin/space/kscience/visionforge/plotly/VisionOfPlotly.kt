package space.kscience.visionforge.plotly

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.dataforge.misc.DFExperimental
import space.kscience.plotly.Plot
import space.kscience.plotly.Plotly
import space.kscience.visionforge.VisionBase
import space.kscience.visionforge.html.VisionOutput
import space.kscience.visionforge.root

@Serializable
@SerialName("vision.plotly")
public class VisionOfPlotly private constructor() : VisionBase() {
    public constructor(plot: Plot) : this() {
        properties = plot.config
    }

    public val plot: Plot get() = Plot(properties ?: Config())
}

public fun Plot.asVision(): VisionOfPlotly = VisionOfPlotly(this)

@DFExperimental
public inline fun VisionOutput.plotly(
    block: Plot.() -> Unit,
): VisionOfPlotly = VisionOfPlotly(Plotly.plot(block)).apply {
    root(this@plotly.manager)
}