package space.kscience.visionforge.markup

import kotlinx.serialization.modules.SerializersModule
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.PluginFactory
import space.kscience.dataforge.context.PluginTag
import space.kscience.dataforge.meta.Meta
import space.kscience.visionforge.VisionPlugin

public expect class MarkupPlugin: VisionPlugin{

    override val tag: PluginTag
    override val visionSerializersModule: SerializersModule

    public companion object : PluginFactory<MarkupPlugin>{
        override val tag: PluginTag

        override fun build(context: Context, meta: Meta): MarkupPlugin
    }
}