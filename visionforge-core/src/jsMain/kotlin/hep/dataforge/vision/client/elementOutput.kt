package hep.dataforge.vision.client

import hep.dataforge.meta.DFExperimental
import hep.dataforge.names.Name
import hep.dataforge.names.toName
import hep.dataforge.provider.Type
import hep.dataforge.vision.Vision
import hep.dataforge.vision.html.BindingHtmlOutputScope
import hep.dataforge.vision.html.HtmlOutputScope
import hep.dataforge.vision.html.HtmlVisionFragment
import kotlinx.browser.document
import kotlinx.html.TagConsumer
import org.w3c.dom.*

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
    public fun render(element: Element, vision: Vision): Unit

    public companion object {
        public const val TYPE: String = "elementVisionRenderer"
        public const val ZERO_RATING: Int = 0
        public const val DEFAULT_RATING: Int = 10
    }
}

@DFExperimental
public fun Map<String, Vision>.bind(rendererFactory: (Vision) -> ElementVisionRenderer) {
    forEach { (id, vision) ->
        val element = document.getElementById(id) ?: error("Could not find element with id $id")
        rendererFactory(vision).render(element, vision)
    }
}

@DFExperimental
public fun Element.renderAllVisions(visionProvider: (Name) -> Vision, rendererFactory: (Vision) -> ElementVisionRenderer) {
    val elements = getElementsByClassName(HtmlOutputScope.OUTPUT_CLASS)
    elements.asList().forEach { element ->
        val name = element.attributes[HtmlOutputScope.OUTPUT_NAME_ATTRIBUTE]?.value
        if (name == null) {
            console.error("Attribute ${HtmlOutputScope.OUTPUT_NAME_ATTRIBUTE} not defined in the output element")
            return@forEach
        }
        val vision = visionProvider(name.toName())
        rendererFactory(vision).render(element, vision)
    }
}

@DFExperimental
public fun Document.renderAllVisions(visionProvider: (Name) -> Vision, rendererFactory: (Vision) -> ElementVisionRenderer): Unit {
    documentElement?.renderAllVisions(visionProvider,rendererFactory)
}

@DFExperimental
public fun HtmlVisionFragment<Vision>.renderInDocument(
    root: TagConsumer<HTMLElement>,
    renderer: ElementVisionRenderer,
): HTMLElement = BindingHtmlOutputScope<HTMLElement, Vision>(root).apply(content).let { scope ->
    scope.finalize().apply {
        scope.bindings.forEach { (name, vision) ->
            val id = scope.resolveId(name)
            val element = document.getElementById(id) ?: error("Could not find element with name $name and id $id")
            renderer.render(element, vision)
        }
    }
}
