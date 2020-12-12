package hep.dataforge.vision.html

import kotlinx.html.FlowContent
import kotlinx.html.TagConsumer

public typealias HtmlFragment = TagConsumer<*>.()->Unit

public fun TagConsumer<*>.fragment(fragment: HtmlFragment) {
    fragment()
}

public fun FlowContent.fragment(fragment: HtmlFragment) {
    fragment(consumer)
}

public typealias HtmlVisionFragment = VisionTagConsumer<*>.() -> Unit