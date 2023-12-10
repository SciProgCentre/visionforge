package space.kscience.visionforge.bootstrap

import org.w3c.dom.Element
import react.RBuilder
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.dataforge.meta.isEmpty
import space.kscience.visionforge.Vision
import space.kscience.visionforge.getStyle
import space.kscience.visionforge.react.EditorPropertyState
import space.kscience.visionforge.react.PropertyEditor
import space.kscience.visionforge.react.metaViewer
import space.kscience.visionforge.react.render
import space.kscience.visionforge.root
import space.kscience.visionforge.solid.SolidReference
import space.kscience.visionforge.styles

public fun RBuilder.visionPropertyEditor(
    vision: Vision,
    descriptor: MetaDescriptor? = vision.descriptor,
    key: Any? = null,
) {

    card("Properties") {
        child(PropertyEditor) {
            attrs {
                this.key = key?.toString()
                this.meta = vision.properties.root()
                this.updates = vision.properties.changes
                this.descriptor = descriptor
                this.scope = vision.manager?.context ?: error("Orphan vision could not be observed")
                this.getPropertyState = { name ->
                    val ownMeta = vision.properties.own?.get(name)
                    if (ownMeta != null && !ownMeta.isEmpty()) {
                        EditorPropertyState.Defined
                    } else if (vision.properties.root().getValue(name) != null) {
                        // TODO differentiate
                        EditorPropertyState.Default()
                    } else {
                        EditorPropertyState.Undefined
                    }
                }
            }
        }
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
): Unit = space.kscience.visionforge.react.createRoot(this).render {
    visionPropertyEditor(item, descriptor = descriptor)
}