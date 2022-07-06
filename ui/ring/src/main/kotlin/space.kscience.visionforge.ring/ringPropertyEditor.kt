package space.kscience.visionforge.ring

import org.w3c.dom.Element
import react.RBuilder
import react.dom.client.createRoot
import react.dom.p
import ringui.Island
import ringui.SmartTabs
import ringui.Tab
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.visionforge.Vision
import space.kscience.visionforge.computeProperties
import space.kscience.visionforge.getStyle
import space.kscience.visionforge.react.flexColumn
import space.kscience.visionforge.react.metaViewer
import space.kscience.visionforge.react.propertyEditor
import space.kscience.visionforge.react.render
import space.kscience.visionforge.solid.SolidReference
import space.kscience.visionforge.styles

public fun RBuilder.ringPropertyEditor(
    vision: Vision,
    descriptor: MetaDescriptor? = vision.descriptor,
    key: Any? = null,
) {
    val styles = if (vision is SolidReference) {
        (vision.styles + vision.prototype.styles).distinct()
    } else {
        vision.styles
    }

    flexColumn {
        Island("Properties") {
            propertyEditor(
                ownProperties = vision.meta,
                allProperties = vision.computeProperties(),
                descriptor = descriptor,
                key = key
            )
        }

        if (styles.isNotEmpty()) {
            Island("Styles") {
                if (styles.size == 1) {
                    val styleName = styles.first()
                    p {
                        +styleName
                    }
                    val style = vision.getStyle(styleName)
                    if (style != null) {
                        Tab(styleName, id = styleName) {
                            metaViewer(style)
                        }
                    }
                } else {
                    SmartTabs {
                        styles.forEach { styleName ->
                            val style = vision.getStyle(styleName)
                            if (style != null) {
                                Tab(styleName, id = styleName) {
                                    metaViewer(style)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


public fun Element.ringPropertyEditor(
    item: Vision,
    descriptor: MetaDescriptor? = item.descriptor,
): Unit = createRoot(this).render {
    ringPropertyEditor(item, descriptor = descriptor)
}