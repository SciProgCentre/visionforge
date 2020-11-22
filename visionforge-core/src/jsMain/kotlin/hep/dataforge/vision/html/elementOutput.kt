package hep.dataforge.vision.html

import hep.dataforge.names.Name
import hep.dataforge.names.toName
import hep.dataforge.vision.Vision
import kotlinx.browser.document
import kotlinx.html.TagConsumer
import org.w3c.dom.*

public interface ElementVisionRenderer<in V : Vision> {
    public fun render(element: Element, vision: V): Unit
}

public fun <V : Vision> Map<String, V>.bind(renderer: ElementVisionRenderer<V>) {
    forEach { (id, vision) ->
        val element = document.getElementById(id) ?: error("Could not find element with id $id")
        renderer.render(element, vision)
    }
}

public fun <V : Vision> Element.renderVisions(renderer: ElementVisionRenderer<V>, visionProvider: (Name) -> V?) {
    val elements = getElementsByClassName(HtmlOutputScope.OUTPUT_CLASS)
    elements.asList().forEach { element ->
        val name = element.attributes[HtmlOutputScope.NAME_ATTRIBUTE]?.value
        if (name == null) {
            console.error("Attribute ${HtmlOutputScope.NAME_ATTRIBUTE} not defined in the output element")
            return@forEach
        }
        val vision = visionProvider(name.toName())
        if (vision == null) {
            console.error("Vision with name $name is not resolved")
            return@forEach
        }
        renderer.render(element, vision)
    }
}

public fun <V : Vision> Document.renderVisions(renderer: ElementVisionRenderer<V>, visionProvider: (Name) -> V?): Unit {
    documentElement?.renderVisions(renderer, visionProvider)
}

public fun HtmlVisionFragment<Vision>.renderInDocument(
    root: TagConsumer<HTMLElement>,
    renderer: ElementVisionRenderer<Vision>,
): HTMLElement = BindingHtmlOutputScope<HTMLElement, Vision>(root).apply(content).let { scope ->
    scope.finalize().apply {
        scope.bindings.forEach { (name, vision) ->
            val id = scope.resolveId(name)
            val element = document.getElementById(id) ?: error("Could not find element with name $name and id $id")
            renderer.render(element, vision)
        }
    }
}
