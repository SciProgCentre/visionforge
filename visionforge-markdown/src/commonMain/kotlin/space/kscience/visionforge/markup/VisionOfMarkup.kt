package space.kscience.visionforge.markup

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import space.kscience.dataforge.meta.string
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.asName
import space.kscience.visionforge.AbstractVision
import space.kscience.visionforge.Vision
import space.kscience.visionforge.VisionBuilder
import space.kscience.visionforge.html.VisionOutput
import space.kscience.visionforge.root

@Serializable
@SerialName("vision.markup")
public class VisionOfMarkup(
    public val format: String = COMMONMARK_FORMAT,
) : AbstractVision() {

    //TODO add templates

    public var content: String? by properties.root().string(CONTENT_PROPERTY_KEY)

    public companion object {
        public val CONTENT_PROPERTY_KEY: Name = "content".asName()
        public const val COMMONMARK_FORMAT: String = "markdown.commonmark"
        public const val GFM_FORMAT: String = "markdown.gfm"
    }
}

//language = markdown
public fun VisionOfMarkup.content(text: () -> String) {
    content = text()
}

internal val markupSerializersModule = SerializersModule {
    polymorphic(Vision::class) {
        subclass(VisionOfMarkup.serializer())
    }
}

/**
 * Embed a dynamic markdown block in a vision
 */
@VisionBuilder
public inline fun VisionOutput.markdown(
    format: String = VisionOfMarkup.COMMONMARK_FORMAT,
    block: VisionOfMarkup.() -> Unit,
): VisionOfMarkup {
    requirePlugin(MarkupPlugin)
    return VisionOfMarkup(format).apply(block)
}