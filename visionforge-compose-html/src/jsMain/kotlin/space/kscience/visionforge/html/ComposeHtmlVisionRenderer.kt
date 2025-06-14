package space.kscience.visionforge.html

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.css.Style
import org.jetbrains.compose.web.dom.DOMScope
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.Element
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.names.Name
import space.kscience.visionforge.Vision
import kotlin.reflect.cast

/**
 * An [ElementVisionRenderer] that could be used directly in Compose-html as well as a stand-alone renderer
 */
public interface ComposeHtmlVisionRenderer : ElementVisionRenderer {

    @Composable
    public fun DOMScope<Element>.render(name: Name, vision: Vision, meta: Meta)

    override fun render(element: Element, name: Name, vision: Vision, meta: Meta) {
        renderComposable(element) {
            Style(VisionForgeStyles)
            render(name, vision, meta)
        }
    }

    public companion object
}

/**
 * A compose-html renderer for a vision of a given type
 */
public inline fun <reified T : Vision> ComposeHtmlVisionRenderer(
    acceptRating: Int = ElementVisionRenderer.DEFAULT_RATING,
    noinline renderFunction: @Composable DOMScope<Element>.(name: Name, vision: T, meta: Meta) -> Unit,
): ComposeHtmlVisionRenderer = object : ComposeHtmlVisionRenderer {

    override fun rateVision(vision: Vision): Int =
        if (vision::class == T::class) acceptRating else ElementVisionRenderer.ZERO_RATING

    @Composable
    override fun DOMScope<Element>.render(name: Name, vision: Vision, meta: Meta) {
        renderFunction(name, T::class.cast(vision), meta)
    }

    override fun toString(): String = "ElementVisionRender(${T::class.simpleName})"
}
