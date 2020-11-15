package hep.dataforge.vision.rendering

import hep.dataforge.vision.Renderer
import hep.dataforge.vision.Vision
import org.w3c.dom.HTMLElement

/**
 * A display container factory for specific vision
 * @param V type of [Vision] to be rendered
 * @param C the specific type of the container
 */
public fun interface HTMLVisionDisplay<in V : Vision, C : Renderer<V>> {
    public fun attachRenderer(element: HTMLElement): C
}

/**
 * Render a specific element and return container for configuration
 */
public fun <V : Vision, C : Renderer<V>> HTMLVisionDisplay<V, C>.render(element: HTMLElement, vision: V): C =
    attachRenderer(element).apply { render(vision)}
