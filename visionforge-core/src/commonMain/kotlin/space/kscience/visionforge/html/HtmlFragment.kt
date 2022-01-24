package space.kscience.visionforge.html

import kotlinx.html.FlowContent
import kotlinx.html.TagConsumer
import kotlinx.html.stream.createHTML

public typealias HtmlFragment = TagConsumer<*>.() -> Unit

public fun HtmlFragment.renderToString(): String = createHTML().apply(this).finalize()

public fun TagConsumer<*>.fragment(fragment: HtmlFragment) {
    fragment()
}

public fun FlowContent.fragment(fragment: HtmlFragment) {
    fragment(consumer)
}

public operator fun HtmlFragment.plus(other: HtmlFragment): HtmlFragment = {
    this@plus()
    other()
}