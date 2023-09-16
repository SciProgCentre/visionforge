package space.kscience.visionforge.html

import kotlinx.html.FlowContent
import kotlinx.html.TagConsumer
import kotlinx.html.stream.createHTML

/**
 * A standalone HTML fragment
 */
public fun interface HtmlFragment {
    public fun TagConsumer<*>.append()
}

/**
 * Convenience method to append fragment to the given [consumer]
 */
public fun HtmlFragment.appendTo(consumer: TagConsumer<*>): Unit = consumer.append()

/**
 * Create a string from this [HtmlFragment]
 */
public fun HtmlFragment.renderToString(): String = createHTML().apply { append() }.finalize()

public fun TagConsumer<*>.appendFragment(fragment: HtmlFragment): Unit = fragment.appendTo(this)

public fun FlowContent.appendFragment(fragment: HtmlFragment): Unit = fragment.appendTo(consumer)

public operator fun HtmlFragment.plus(other: HtmlFragment): HtmlFragment = HtmlFragment {
    this@plus.appendTo(this)
    other.appendTo(this)
}