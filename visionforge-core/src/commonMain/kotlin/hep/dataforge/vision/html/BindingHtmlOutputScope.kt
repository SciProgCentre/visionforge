package hep.dataforge.vision.html

import hep.dataforge.names.Name
import hep.dataforge.vision.Vision
import kotlinx.html.TagConsumer

public class BindingHtmlOutputScope<T, V : Vision>(
    root: TagConsumer<T>,
    prefix: String? = null,
) : HtmlOutputScope<T, V>(root,prefix) {

    private val _bindings = HashMap<Name, V>()
    public val bindings: Map<Name, V> get() = _bindings

    override fun renderVision(htmlOutput: HtmlOutput<V>, vision: V) {
        _bindings[htmlOutput.name] = vision
    }
}