package space.kscience.visionforge

import kotlinx.dom.clear
import kotlinx.html.TagConsumer
import kotlinx.html.dom.append
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.serializerOrNull
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.misc.Named
import space.kscience.dataforge.misc.Type
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.asName
import space.kscience.dataforge.names.parseAsName
import kotlin.reflect.KClass
import kotlin.reflect.cast

/**
 * A browser renderer for a [Vision].
 */
@Type(ElementVisionRenderer.TYPE)
public interface ElementVisionRenderer : Named {

    /**
     * Give a [vision] integer rating based on this renderer capabilities. [ZERO_RATING] or negative values means that this renderer
     * can't process a vision. The value of [DEFAULT_RATING] used for default renderer. Specialized renderers could specify
     * higher value in order to "steal" rendering job
     */
    public fun rateVision(vision: Vision): Int

    /**
     * Display the [vision] inside a given [element] replacing its current content.
     * @param meta additional parameters for rendering container
     */
    public fun render(element: Element, vision: Vision, meta: Meta = Meta.EMPTY)

    public companion object {
        public const val TYPE: String = "elementVisionRenderer"
        public const val ZERO_RATING: Int = 0
        public const val DEFAULT_RATING: Int = 10
    }
}

/**
 * A browser renderer for element of given type
 */
public class SingleTypeVisionRenderer<T : Vision>(
    public val kClass: KClass<T>,
    private val acceptRating: Int = ElementVisionRenderer.DEFAULT_RATING,
    private val renderFunction: TagConsumer<HTMLElement>.(vision: T, meta: Meta) -> Unit,
) : ElementVisionRenderer {

    @OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
    override val name: Name
        get() = kClass.serializerOrNull()?.descriptor?.serialName?.parseAsName()
            ?: kClass.toString().asName()

    override fun rateVision(vision: Vision): Int =
        if (vision::class == kClass) acceptRating else ElementVisionRenderer.ZERO_RATING

    override fun render(element: Element, vision: Vision, meta: Meta) {
        element.clear()
        element.append {
            renderFunction(kClass.cast(vision), meta)
        }
    }
}

public inline fun <reified T : Vision> ElementVisionRenderer(
    acceptRating: Int = ElementVisionRenderer.DEFAULT_RATING,
    noinline renderFunction: TagConsumer<HTMLElement>.(vision: T, meta: Meta) -> Unit,
): ElementVisionRenderer = SingleTypeVisionRenderer(T::class, acceptRating, renderFunction)
