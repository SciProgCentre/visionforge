package hep.dataforge.vision.html

import hep.dataforge.misc.DFExperimental
import hep.dataforge.vision.VisionForge
import kotlinx.html.FlowContent
import kotlinx.html.TagConsumer

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