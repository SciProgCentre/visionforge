package space.kscience.visionforge.plotly

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import space.kscience.dataforge.meta.MutableMeta
import space.kscience.dataforge.meta.MutableMetaSerializer
import space.kscience.dataforge.meta.ObservableMeta
import space.kscience.dataforge.meta.asObservable
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.dataforge.names.Name
import space.kscience.plotly.Plot
import space.kscience.plotly.Plotly
import space.kscience.plotly.PlotlyConfig
import space.kscience.visionforge.AbstractVisionProperties
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

    public val plot: Plot get() = Plot(meta.asObservable())

    @Transient
    override var parent: Vision? = null

    @Transient
    override val properties: MutableVisionProperties = object : AbstractVisionProperties(this, meta) {

        override fun flowChanges(): Flow<Name> = if (meta is ObservableMeta) {
            callbackFlow {
                meta.onChange(this) {
                    launch {
                        send(it)
                    }
                }
                awaitClose {
                    meta.removeListener(this)
                }
            }
        } else emptyFlow()


        override fun invalidate(propertyName: Name) {
            // Do nothing
        }

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