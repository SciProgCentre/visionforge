package space.kscience.visionforge

import org.w3c.dom.Element
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.misc.Type

@Type(ElementVisionRenderer.TYPE)
public interface ElementVisionRenderer {

    /**
     * Give a [vision] integer rating based on this renderer capabilities. [ZERO_RATING] or negative values means that this renderer
     * can't process a vision. The value of [DEFAULT_RATING] used for default renderer. Specialized renderers could specify
     * higher value in order to "steal" rendering job
     */
    public fun rateVision(vision: Vision): Int

    /**
     * Display the [vision] inside a given [element] replacing its current content
     */
    public fun render(element: Element, vision: Vision, meta: Meta = Meta.EMPTY)

    public companion object {
        public const val TYPE: String = "elementVisionRenderer"
        public const val ZERO_RATING: Int = 0
        public const val DEFAULT_RATING: Int = 10
    }
}