package space.kscience.visionforge.plotly

import kotlinx.serialization.modules.SerializersModule
import org.w3c.dom.Element
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.PluginFactory
import space.kscience.dataforge.context.PluginTag
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.asName
import space.kscience.plotly.PlotlyConfig
import space.kscience.plotly.plot
import space.kscience.visionforge.ElementVisionRenderer
import space.kscience.visionforge.Vision
import space.kscience.visionforge.VisionClient
import space.kscience.visionforge.VisionPlugin
import kotlin.reflect.KClass

public actual class PlotlyPlugin : VisionPlugin(), ElementVisionRenderer {
    public val visionClient: VisionClient by require(VisionClient)

    override val tag: PluginTag get() = Companion.tag

    override val visionSerializersModule: SerializersModule get() = plotlySerializersModule

    override fun rateVision(vision: Vision): Int = when (vision) {
        is VisionOfPlotly -> ElementVisionRenderer.DEFAULT_RATING
        else -> ElementVisionRenderer.ZERO_RATING
    }

    override fun render(element: Element, vision: Vision, meta: Meta) {
        val plot = (vision as? VisionOfPlotly)?.plot ?: error("VisionOfPlotly expected but ${vision::class} found")
        val config = PlotlyConfig.read(meta)
        println(plot.meta)
        println(plot.data[0].toMeta())
        element.plot(plot, config)
    }

    override fun content(target: String): Map<Name, Any> {
        return when (target) {
            ElementVisionRenderer.TYPE -> mapOf("plotly".asName() to this)
            else -> super.content(target)
        }
    }

    public actual companion object : PluginFactory<PlotlyPlugin> {
        override val tag: PluginTag = PluginTag("vision.plotly", PluginTag.DATAFORGE_GROUP)
        override val type: KClass<PlotlyPlugin> = PlotlyPlugin::class
        override fun invoke(meta: Meta, context: Context): PlotlyPlugin = PlotlyPlugin()
    }
}