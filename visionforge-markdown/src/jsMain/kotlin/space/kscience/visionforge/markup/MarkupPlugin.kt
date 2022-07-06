package space.kscience.visionforge.markup

import kotlinx.browser.document
import kotlinx.dom.clear
import kotlinx.html.dom.append
import kotlinx.serialization.modules.SerializersModule
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.w3c.dom.Element
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.PluginFactory
import space.kscience.dataforge.context.PluginTag
import space.kscience.dataforge.meta.Meta
import space.kscience.visionforge.*
import space.kscience.visionforge.markup.VisionOfMarkup.Companion.COMMONMARK_FORMAT
import space.kscience.visionforge.markup.VisionOfMarkup.Companion.GFM_FORMAT
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
        val flavour = when (vision.format) {
            COMMONMARK_FORMAT -> CommonMarkFlavourDescriptor()
            GFM_FORMAT -> GFMFlavourDescriptor()
            //TODO add new formats via plugins
            else-> error("Format ${vision.format} not recognized")
        }
        vision.useProperty(VisionOfMarkup::content) {
            div.clear()
            div.append {
                markdown(flavour) { vision.content ?: "" }
            }
        }
        element.append(div)
    }

    public companion object : PluginFactory<MarkupPlugin> {
        override val tag: PluginTag = PluginTag("vision.markup", PluginTag.DATAFORGE_GROUP)
        override val type: KClass<MarkupPlugin> = MarkupPlugin::class

        override fun build(context: Context, meta: Meta): MarkupPlugin  = MarkupPlugin()

    }
}