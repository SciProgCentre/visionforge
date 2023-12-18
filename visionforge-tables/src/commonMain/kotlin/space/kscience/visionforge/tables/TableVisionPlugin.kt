package space.kscience.visionforge.tables

import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.PluginFactory
import space.kscience.dataforge.context.PluginTag
import space.kscience.dataforge.meta.Meta
import space.kscience.visionforge.Vision
import space.kscience.visionforge.VisionPlugin

public class TableVisionPlugin : VisionPlugin() {
    override val tag: PluginTag get() = Companion.tag

    override val visionSerializersModule: SerializersModule = SerializersModule {
        polymorphic(Vision::class) {
            subclass(VisionOfTable.serializer())
        }
    }

    public companion object : PluginFactory<TableVisionPlugin> {
        override val tag: PluginTag = PluginTag("vision.table", PluginTag.DATAFORGE_GROUP)

        override fun build(context: Context, meta: Meta): TableVisionPlugin = TableVisionPlugin()
    }
}