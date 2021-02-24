package hep.dataforge.vision.plotly

import hep.dataforge.context.Context
import hep.dataforge.context.PluginFactory
import hep.dataforge.context.PluginTag
import hep.dataforge.meta.Meta
import hep.dataforge.names.Name
import hep.dataforge.names.asName
import hep.dataforge.vision.Vision
import hep.dataforge.vision.VisionForge
import hep.dataforge.vision.VisionPlugin
import hep.dataforge.vision.client.ElementVisionRenderer
import hep.dataforge.vision.client.VisionClient
import kotlinx.serialization.modules.SerializersModule
import kscience.plotly.PlotlyConfig
import kscience.plotly.plot
import org.w3c.dom.Element
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
        element.plot(plot, config)
    }

    override fun content(target: String): Map<Name, Any> {
        return when (target) {
            ElementVisionRenderer.TYPE -> mapOf("plotly".asName() to this)
            else -> super.content(target)
        }
    }

    public companion object : PluginFactory<PlotlyPlugin> {
        override val tag: PluginTag = PluginTag("vision.plotly", PluginTag.DATAFORGE_GROUP)
        override val type: KClass<PlotlyPlugin> = PlotlyPlugin::class
        override fun invoke(meta: Meta, context: Context): PlotlyPlugin = PlotlyPlugin()
    }
}

/**
 * Ensure that [PlotlyPlugin] is loaded in the global [VisionForge] context
 */
@JsExport
public fun usePlotly() {
    VisionForge.plugins.fetch(PlotlyPlugin)
}