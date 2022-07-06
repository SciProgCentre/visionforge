package space.kscience.visionforge.plotly

import kotlinx.serialization.modules.SerializersModule
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.PluginFactory
import space.kscience.dataforge.context.PluginTag
import space.kscience.dataforge.meta.Meta
import space.kscience.visionforge.VisionPlugin
import kotlin.reflect.KClass

public actual class PlotlyPlugin : VisionPlugin() {

    override val tag: PluginTag get() = Companion.tag

    override val visionSerializersModule: SerializersModule get() = plotlySerializersModule

    public actual companion object : PluginFactory<PlotlyPlugin> {
        override val tag: PluginTag = PluginTag("vision.plotly", PluginTag.DATAFORGE_GROUP)
        override val type: KClass<PlotlyPlugin> = PlotlyPlugin::class

        override fun build(context: Context, meta: Meta): PlotlyPlugin = PlotlyPlugin()

    }
}