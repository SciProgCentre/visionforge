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
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.asName
import space.kscience.visionforge.Vision
import space.kscience.visionforge.VisionPlugin
import space.kscience.visionforge.html.ElementVisionRenderer
import space.kscience.visionforge.html.JsVisionClient
import space.kscience.visionforge.markup.VisionOfMarkup.Companion.COMMONMARK_FORMAT
import space.kscience.visionforge.markup.VisionOfMarkup.Companion.GFM_FORMAT
import space.kscience.visionforge.useProperty

public actual class MarkupPlugin : VisionPlugin(), ElementVisionRenderer {
    public val visionClient: JsVisionClient by require(JsVisionClient)
    override val tag: PluginTag get() = Companion.tag
    override val visionSerializersModule: SerializersModule get() = markupSerializersModule

    override fun rateVision(vision: Vision): Int = when (vision) {
        is VisionOfMarkup -> ElementVisionRenderer.DEFAULT_RATING
        else -> ElementVisionRenderer.ZERO_RATING
    }

    override fun render(element: Element, name: Name, vision: Vision, meta: Meta) {
        require(vision is VisionOfMarkup) { "The vision is not a markup vision" }
        val div = document.createElement("div")
        val flavour = when (vision.format) {
            COMMONMARK_FORMAT -> CommonMarkFlavourDescriptor()
            GFM_FORMAT -> GFMFlavourDescriptor()
            //TODO add new formats via plugins
            else -> error("Format ${vision.format} not recognized")
        }
        vision.useProperty(VisionOfMarkup::content) {
            div.clear()
            div.append {
                markdown(flavour) { vision.content ?: "" }
            }
        }
        element.append(div)
    }

    override fun toString(): String = "Markup"

    override fun content(target: String): Map<Name, Any> = when (target) {
        ElementVisionRenderer.TYPE -> mapOf("markup".asName() to this)
        else -> super.content(target)
    }

    public actual companion object : PluginFactory<MarkupPlugin> {
        override val tag: PluginTag = PluginTag("vision.markup.js", PluginTag.DATAFORGE_GROUP)

        override fun build(context: Context, meta: Meta): MarkupPlugin = MarkupPlugin()

    }
}