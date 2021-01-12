package hep.dataforge.vision.plotly

import hep.dataforge.context.Context
import hep.dataforge.context.Plugin
import hep.dataforge.context.PluginFactory
import hep.dataforge.context.PluginTag
import hep.dataforge.meta.Meta
import hep.dataforge.vision.VisionPlugin
import kotlinx.serialization.modules.SerializersModule
import kotlin.reflect.KClass

public actual class PlotlyPlugin : VisionPlugin(), Plugin {

    override val tag: PluginTag get() = Companion.tag

    override val visionSerializersModule: SerializersModule get() = plotlySerializersModule

    public companion object : PluginFactory<PlotlyPlugin> {
        override val tag: PluginTag = PluginTag("vision.plotly", PluginTag.DATAFORGE_GROUP)
        override val type: KClass<PlotlyPlugin> = PlotlyPlugin::class
        override fun invoke(meta: Meta, context: Context): PlotlyPlugin = PlotlyPlugin()
    }
}

public fun Context.withPlotly(): Context = apply {
    plugins.fetch(PlotlyPlugin)
}