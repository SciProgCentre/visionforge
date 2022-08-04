package space.kscience.visionforge.plotly

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.dataforge.meta.asObservable
import space.kscience.dataforge.misc.DFExperimental
import space.kscience.dataforge.names.Name
import space.kscience.plotly.Plot
import space.kscience.plotly.Plotly
import space.kscience.visionforge.VisionGroup
import space.kscience.visionforge.getProperty
import space.kscience.visionforge.html.VisionOutput

@Serializable
@SerialName("vision.plotly")
public class VisionOfPlotly private constructor() : VisionGroup() {

    public constructor(plot: Plot) : this() {
        setProperty(Name.EMPTY, plot.meta)
    }

    public val plot: Plot get() = Plot(getProperty(Name.EMPTY).asObservable())
}

public fun Plot.asVision(): VisionOfPlotly = VisionOfPlotly(this)

@DFExperimental
public inline fun VisionOutput.plotly(
    block: Plot.() -> Unit,
): VisionOfPlotly {
    requirePlugin(PlotlyPlugin)
    return VisionOfPlotly(Plotly.plot(block))
}