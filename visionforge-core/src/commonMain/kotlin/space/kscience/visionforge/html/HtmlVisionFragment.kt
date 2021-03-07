package space.kscience.visionforge.html

import kotlinx.html.FlowContent
import kotlinx.html.TagConsumer
import space.kscience.dataforge.misc.DFExperimental
import space.kscience.visionforge.VisionForge

public typealias HtmlFragment = TagConsumer<*>.() -> Unit

public fun TagConsumer<*>.fragment(fragment: HtmlFragment) {
    fragment()
}

public fun FlowContent.fragment(fragment: HtmlFragment) {
    fragment(consumer)
}

public typealias HtmlVisionFragment = VisionTagConsumer<*>.() -> Unit

@DFExperimental
public fun VisionForge.fragment(content: HtmlVisionFragment): VisionTagConsumer<*>.() -> Unit = content