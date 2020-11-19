package hep.dataforge.vision.html

import hep.dataforge.vision.Vision
import kotlinx.browser.document
import kotlinx.html.TagConsumer
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement

public interface HtmlVisionBinding<in V: Vision>{
    public fun bind(element: Element, vision: V): Unit
}

public fun <V: Vision> Map<String, V>.bind(binder: HtmlVisionBinding<V>){
    forEach { (id, vision) ->
        val element = document.getElementById(id) ?: error("Could not find element with id $id")
        binder.bind(element, vision)
    }
}

public fun HtmlVisionFragment<Vision>.bindToDocument(
    root: TagConsumer<HTMLElement>,
    binder: HtmlVisionBinding<Vision>,
): HTMLElement = BindingHtmlOutputScope<HTMLElement, Vision>(root).apply(layout).let { scope ->
    scope.finalize().apply {
        scope.bindings.forEach { (name, vision) ->
            val id = scope.resolveId(name)
            val element = document.getElementById(id) ?: error("Could not find element with name $name and id $id")
            binder.bind(element, vision)
        }
    }
}
