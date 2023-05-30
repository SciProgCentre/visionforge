package space.kscience.visionforge.markup

import kotlinx.serialization.modules.SerializersModule
import org.intellij.lang.annotations.Language
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.PluginFactory
import space.kscience.dataforge.context.PluginTag
import space.kscience.dataforge.meta.Meta
import space.kscience.visionforge.VisionPlugin

public actual class MarkupPlugin : VisionPlugin() {
    override val visionSerializersModule: SerializersModule get() = markupSerializersModule

    override val tag: PluginTag get() = Companion.tag

    public actual companion object : PluginFactory<MarkupPlugin> {
        override val tag: PluginTag = PluginTag("vision.markup", PluginTag.DATAFORGE_GROUP)

        override fun build(context: Context, meta: Meta): MarkupPlugin = MarkupPlugin()

    }
}

public fun VisionOfMarkup.content(@Language("markdown") text: String) {
    content = text
}