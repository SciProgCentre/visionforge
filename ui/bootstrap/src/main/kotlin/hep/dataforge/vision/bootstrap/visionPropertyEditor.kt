package hep.dataforge.vision.bootstrap

import hep.dataforge.meta.descriptors.NodeDescriptor
import hep.dataforge.vision.*
import hep.dataforge.vision.react.metaViewer
import hep.dataforge.vision.react.propertyEditor
        import hep.dataforge.vision.solid.SolidReference
import org.w3c.dom.Element
import react.RBuilder
import react.dom.render

public fun RBuilder.visionPropertyEditor(
    vision: Vision,
    descriptor: NodeDescriptor? = vision.descriptor,
    key: Any? = null,
) {

    card("Properties") {
        propertyEditor(
            ownProperties = vision.ownProperties,
            allProperties = vision.allProperties(),
            updateFlow = vision.propertyChanges,
            descriptor = descriptor,
            key = key
        )
    }
    val styles = if (vision is SolidReference) {
        (vision.styles + vision.prototype.styles).distinct()
    } else {
        vision.styles
    }
    if (styles.isNotEmpty()) {
        card("Styles") {
            accordion("styles") {
                styles.forEach { styleName ->
                    val style = vision.getStyle(styleName)
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
): Unit = render(this) {
    visionPropertyEditor(item, descriptor = descriptor)
}