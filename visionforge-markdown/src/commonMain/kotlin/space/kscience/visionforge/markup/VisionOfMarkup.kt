package space.kscience.visionforge.markup

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import space.kscience.dataforge.meta.string
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.asName
import space.kscience.visionforge.Vision
import space.kscience.visionforge.VisionBase
import space.kscience.visionforge.setProperty

@Serializable
@SerialName("vision.markup")
public class VisionOfMarkup(
    public val format: String = COMMONMARK_FORMAT
) : VisionBase() {

    //TODO add templates

    public var content: String?
        get() = getOwnProperty(CONTENT_PROPERTY_KEY).string
        set(value) {
            setProperty(CONTENT_PROPERTY_KEY, value)
        }

    public companion object {
        public val CONTENT_PROPERTY_KEY: Name = "content".asName()
        public const val COMMONMARK_FORMAT: String = "markdown.commonmark"
        public const val GFM_FORMAT: String = "markdown.gfm"
    }
}

internal val markupSerializersModule = SerializersModule {
    polymorphic(Vision::class) {
        subclass(VisionOfMarkup.serializer())
    }
}