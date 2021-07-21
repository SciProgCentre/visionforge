package space.kscience.visionforge.markup

import kotlinx.browser.document
import kotlinx.serialization.modules.SerializersModule
import org.w3c.dom.Element
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.PluginFactory
import space.kscience.dataforge.context.PluginTag
import space.kscience.dataforge.meta.Meta
import space.kscience.visionforge.ElementVisionRenderer
import space.kscience.visionforge.Vision
import space.kscience.visionforge.VisionClient
import space.kscience.visionforge.VisionPlugin
import kotlin.reflect.KClass

public class MarkupPlugin : VisionPlugin(), ElementVisionRenderer {
    public val visionClient: VisionClient by require(VisionClient)
    override val tag: PluginTag get() = Companion.tag
    override val visionSerializersModule: SerializersModule get() = markupSerializersModule

    override fun rateVision(vision: Vision): Int = when (vision) {
        is VisionOfMarkup -> ElementVisionRenderer.DEFAULT_RATING
        else -> ElementVisionRenderer.ZERO_RATING
    }

    override fun render(element: Element, vision: Vision, meta: Meta) {
        require(vision is VisionOfMarkup) { "The vision is not a markup vision" }
        val div = document.createElement("div")
        element.append(div)
        TODO()
    }

    public companion object : PluginFactory<MarkupPlugin> {
        override val tag: PluginTag = PluginTag("vision.markup", PluginTag.DATAFORGE_GROUP)
        override val type: KClass<MarkupPlugin> = MarkupPlugin::class
        override fun invoke(meta: Meta, context: Context): MarkupPlugin = MarkupPlugin()
    }
}