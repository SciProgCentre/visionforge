package hep.dataforge.vision.html

import hep.dataforge.meta.Meta
import hep.dataforge.names.Name
import hep.dataforge.vision.Vision
import hep.dataforge.vision.VisionManager
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.script
import kotlinx.html.unsafe


public fun FlowContent.embedVisionFragment(
    manager: VisionManager,
    idPrefix: String? = null,
    fragment: HtmlVisionFragment,
) {
    val consumer = object : VisionTagConsumer<Any?>(consumer, idPrefix) {
        override fun DIV.renderVision(name: Name, vision: Vision, outputMeta: Meta) {
            script {
                type = "text/json"
                attributes["class"] = OUTPUT_DATA_CLASS
                unsafe {
                    +"\n${manager.encodeToString(vision)}\n"
                }
            }
        }
    }
    fragment(consumer)
}

public typealias HtmlVisionRenderer = FlowContent.(name: Name, vision: Vision, meta: Meta) -> Unit

public fun <R> FlowContent.renderVisionFragment(
    renderer: DIV.(name: Name, vision: Vision, meta: Meta) -> Unit,
    idPrefix: String? = null,
    fragment: HtmlVisionFragment,
) {
    val consumer = object : VisionTagConsumer<Any?>(consumer, idPrefix) {
        override fun DIV.renderVision(name: Name, vision: Vision, outputMeta: Meta) = renderer(name, vision, outputMeta)
    }
    fragment(consumer)
}