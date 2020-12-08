package hep.dataforge.vision.html

import hep.dataforge.vision.Vision
import kotlinx.html.FlowContent
import kotlinx.html.TagConsumer

public class HtmlFragment(public val content: TagConsumer<*>.() -> Unit)

public fun TagConsumer<*>.fragment(fragment: HtmlFragment) {
    fragment.content(this)
}

public fun FlowContent.fragment(fragment: HtmlFragment) {
    fragment.content(consumer)
}

public class HtmlVisionFragment<V : Vision>(public val content: OutputTagConsumer<*, V>.() -> Unit)

public fun buildVisionFragment(block: OutputTagConsumer<*, Vision>.() -> Unit): HtmlVisionFragment<Vision> =
    HtmlVisionFragment(block)
