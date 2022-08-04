package space.kscience.visionforge.bootstrap

import org.w3c.dom.Element
import react.RBuilder
import react.dom.client.createRoot
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.visionforge.Vision
import space.kscience.visionforge.computeProperties
import space.kscience.visionforge.getStyle
import space.kscience.visionforge.react.metaViewer
import space.kscience.visionforge.react.propertyEditor
import space.kscience.visionforge.react.render
import space.kscience.visionforge.solid.SolidReference
import space.kscience.visionforge.styles

public fun RBuilder.visionPropertyEditor(
    vision: Vision,
    descriptor: MetaDescriptor? = vision.descriptor,
    key: Any? = null,
) {

    card("Properties") {
        propertyEditor(
            ownProperties = vision.meta,
            allProperties = vision.computeProperties(),
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
    descriptor: MetaDescriptor? = item.descriptor,
): Unit = createRoot(this).render {
    visionPropertyEditor(item, descriptor = descriptor)
}