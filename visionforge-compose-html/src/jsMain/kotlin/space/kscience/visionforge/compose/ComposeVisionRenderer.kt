package space.kscience.visionforge.compose

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.css.Style
import org.jetbrains.compose.web.dom.DOMScope
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.Element
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.names.Name
import space.kscience.visionforge.ElementVisionRenderer
import space.kscience.visionforge.Vision
import space.kscience.visionforge.VisionClient

/**
 * An [ElementVisionRenderer] that could be used directly in Compose-html as well as a stand-alone renderer
 */
public interface ComposeVisionRenderer: ElementVisionRenderer {

    @Composable
    public fun DOMScope<Element>.render(client: VisionClient, name: Name, vision: Vision, meta: Meta)

    override fun render(element: Element, client: VisionClient, name: Name, vision: Vision, meta: Meta) {
        renderComposable(element) {
            Style(VisionForgeStyles)
            render(client, name, vision, meta)
        }
    }
}