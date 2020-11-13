package hep.dataforge.vision.bootstrap

import hep.dataforge.meta.Meta
import hep.dataforge.meta.descriptors.NodeDescriptor
import hep.dataforge.vision.Vision
import hep.dataforge.vision.react.configEditor
import hep.dataforge.vision.react.metaViewer
import hep.dataforge.vision.resolveStyle
import org.w3c.dom.Element
import react.RBuilder
import react.dom.render

public fun RBuilder.visionPropertyEditor(
    item: Vision,
    descriptor: NodeDescriptor? = item.descriptor,
    default: Meta? = null,
    key: Any? = null
) {
    card("Properties") {
        configEditor(item.config, descriptor, default, key)
    }
    val styles = item.styles
    if(styles.isNotEmpty()) {
        card("Styles") {
            accordion("styles") {
                styles.forEach { styleName ->
                    val style = item.resolveStyle(styleName)
                    if (style != null) {
                        entry(styleName) {
                            metaViewer(style)
                        }
                    }
                }
            }
        }
    }
}

public fun Element.visionPropertyEditor(
    item: Vision,
    descriptor: NodeDescriptor? = item.descriptor,
    default: Meta? = null
): Unit = render(this) {
    visionPropertyEditor(item, descriptor, default)
}