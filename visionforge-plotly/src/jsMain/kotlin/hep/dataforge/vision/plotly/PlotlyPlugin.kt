package hep.dataforge.vision.plotly

import hep.dataforge.context.AbstractPlugin
import hep.dataforge.context.Context
import hep.dataforge.context.PluginFactory
import hep.dataforge.context.PluginTag
import hep.dataforge.meta.Meta
import hep.dataforge.vision.Vision
import hep.dataforge.vision.client.ElementVisionRenderer
import kscience.plotly.PlotlyConfig
import kscience.plotly.plot
import org.w3c.dom.Element
import kotlin.reflect.KClass

public class PlotlyPlugin : AbstractPlugin(), ElementVisionRenderer {

    override val tag: PluginTag get() = Companion.tag

    override fun rateVision(vision: Vision): Int =
        if (vision is VisionOfPlotly) ElementVisionRenderer.DEFAULT_RATING else ElementVisionRenderer.ZERO_RATING

    override fun render(element: Element, vision: Vision, meta: Meta) {
        val plot = (vision as? VisionOfPlotly)?.plot ?: error("Only VisionOfPlotly visions are supported")
        val config = PlotlyConfig.read(meta)
        element.plot(plot, config)
    }

    public companion object : PluginFactory<PlotlyPlugin> {
        override val tag: PluginTag = PluginTag("vision.plotly", PluginTag.DATAFORGE_GROUP)
        override val type: KClass<PlotlyPlugin> = PlotlyPlugin::class
        override fun invoke(meta: Meta, context: Context): PlotlyPlugin = PlotlyPlugin()
    }
}