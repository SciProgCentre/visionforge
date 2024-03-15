package space.kscience.visionforge.plotly

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import space.kscience.dataforge.meta.*
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.dataforge.names.Name
import space.kscience.plotly.Plot
import space.kscience.plotly.Plotly
import space.kscience.plotly.PlotlyConfig
import space.kscience.visionforge.MutableVisionProperties
import space.kscience.visionforge.Vision
import space.kscience.visionforge.VisionBuilder
import space.kscience.visionforge.html.VisionOutput

@Serializable
@SerialName("vision.plotly")
public class VisionOfPlotly private constructor(
    @Serializable(MutableMetaSerializer::class) public val meta: MutableMeta,
) : Vision {
    public constructor(plot: Plot) : this(plot.meta)

    @Transient
    public val plot: Plot = Plot(meta.asObservable())

    @Transient
    override var parent: Vision? = null

    @Transient
    override val properties: MutableVisionProperties = object : MutableVisionProperties {
        override val own: Meta get() = plot.meta

        override val changes = callbackFlow {
            plot.meta.onChange(this) {
                launch {
                    send(it)
                }
            }
            awaitClose {
                plot.meta.removeListener(this)
            }
        }

        override fun invalidate(propertyName: Name) {
            //do nothing, updates to source already counted
//            manager?.context?.launch {
//                changes.emit(propertyName)
//            }
        }

        override fun getValue(name: Name, inherit: Boolean?, includeStyles: Boolean?): Value? = plot.meta[name]?.value

        override fun set(name: Name, item: Meta?, notify: Boolean) {
            plot.meta[name] = item
            if (notify) invalidate(name)
        }

        override fun setValue(name: Name, value: Value?, notify: Boolean) {
            plot.meta[name] = value
            if (notify) invalidate(name)
        }

        override val descriptor: MetaDescriptor get() = plot.descriptor
    }


    override val descriptor: MetaDescriptor = Plot.descriptor
}

public fun Plot.asVision(): VisionOfPlotly = VisionOfPlotly(this)

/**
 * Embed a dynamic plotly plot in a vision
 */
@VisionBuilder
public inline fun VisionOutput.plotly(
    config: PlotlyConfig = PlotlyConfig(),
    block: Plot.() -> Unit,
): VisionOfPlotly {
    requirePlugin(PlotlyPlugin)
    meta = config.meta
    return VisionOfPlotly(Plotly.plot(block))
}