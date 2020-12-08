package hep.dataforge.vision.html

import hep.dataforge.names.Name
import hep.dataforge.vision.Vision
import kotlinx.html.FlowContent
import kotlinx.html.TagConsumer

public class BindingOutputTagConsumer<T, V : Vision>(
    root: TagConsumer<T>,
    prefix: String? = null,
) : OutputTagConsumer<T, V>(root, prefix) {

    private val _bindings = HashMap<Name, V>()
    public val bindings: Map<Name, V> get() = _bindings

    override fun FlowContent.renderVision(name: Name, vision: V) {
        _bindings[name] = vision
    }
}

public fun <T : Any> TagConsumer<T>.visionFragment(fragment: HtmlVisionFragment<Vision>): Map<Name, Vision> {
    return BindingOutputTagConsumer<T, Vision>(this).apply(fragment.content).bindings
}

public fun FlowContent.visionFragment(fragment: HtmlVisionFragment<Vision>): Map<Name, Vision> {
    return BindingOutputTagConsumer<Any?, Vision>(consumer).apply(fragment.content).bindings
}