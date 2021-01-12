package hep.dataforge.vision.html

import hep.dataforge.meta.Meta
import hep.dataforge.names.Name
import hep.dataforge.vision.Vision
import hep.dataforge.vision.VisionManager
import kotlinx.html.*


public fun TagConsumer<*>.embedVisionFragment(
    manager: VisionManager,
    idPrefix: String? = null,
    fragment: HtmlVisionFragment,
): Map<Name, Vision> {
    val visionMap = HashMap<Name, Vision>()
    val consumer = object : VisionTagConsumer<Any?>(this@embedVisionFragment, idPrefix) {
        override fun DIV.renderVision(name: Name, vision: Vision, outputMeta: Meta) {
            visionMap[name] = vision
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
    return visionMap
}

public fun FlowContent.embedVisionFragment(
    manager: VisionManager,
    idPrefix: String? = null,
    fragment: HtmlVisionFragment,
): Map<Name, Vision>  = consumer.embedVisionFragment(manager, idPrefix, fragment)

public typealias HtmlVisionRenderer = FlowContent.(name: Name, vision: Vision, meta: Meta) -> Unit

public fun FlowContent.renderVisionFragment(
    renderer: DIV.(name: Name, vision: Vision, meta: Meta) -> Unit,
    idPrefix: String? = null,
    fragment: HtmlVisionFragment,
): Map<Name, Vision> {
    val visionMap = HashMap<Name, Vision>()
    val consumer = object : VisionTagConsumer<Any?>(consumer, idPrefix) {
        override fun DIV.renderVision(name: Name, vision: Vision, outputMeta: Meta) {
            visionMap[name] = vision
            renderer(name, vision, outputMeta)
        }
    }
    fragment(consumer)
    return visionMap
}