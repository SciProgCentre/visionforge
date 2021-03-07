package space.kscience.visionforge.html

import kotlinx.html.*
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.names.Name
import space.kscience.visionforge.Vision
import space.kscience.visionforge.VisionManager


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