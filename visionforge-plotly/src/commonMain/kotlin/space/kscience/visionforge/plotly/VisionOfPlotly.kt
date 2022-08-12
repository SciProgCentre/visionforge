package space.kscience.visionforge.plotly

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.dataforge.meta.asObservable
import space.kscience.dataforge.misc.DFExperimental
import space.kscience.dataforge.names.Name
import space.kscience.plotly.Plot
import space.kscience.plotly.Plotly
import space.kscience.visionforge.AbstractVision
import space.kscience.visionforge.html.VisionOutput
import space.kscience.visionforge.root

@Serializable
@SerialName("vision.plotly")
public class VisionOfPlotly private constructor() : AbstractVision() {

    public constructor(plot: Plot) : this() {
        properties.setProperty(Name.EMPTY, plot.meta)
    }

    public val plot: Plot get() = Plot(properties.root().asObservable())
}

public fun Plot.asVision(): VisionOfPlotly = VisionOfPlotly(this)

@DFExperimental
public inline fun VisionOutput.plotly(
    block: Plot.() -> Unit,
): VisionOfPlotly {
    requirePlugin(PlotlyPlugin)
    return VisionOfPlotly(Plotly.plot(block))
}