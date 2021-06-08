package space.kscience.visionforge.ring

import org.w3c.dom.Element
import react.RBuilder
import react.dom.p
import react.dom.render
import react.useMemo
import ringui.island.ringIsland
import ringui.island.ringIslandContent
import ringui.island.ringIslandHeader
import ringui.tabs.ringSmartTabs
import ringui.tabs.ringTab
import space.kscience.dataforge.meta.descriptors.NodeDescriptor
import space.kscience.visionforge.*
import space.kscience.visionforge.react.flexColumn
import space.kscience.visionforge.react.metaViewer
import space.kscience.visionforge.react.propertyEditor
import space.kscience.visionforge.solid.SolidReference

public fun RBuilder.ringPropertyEditor(
    vision: Vision,
    descriptor: NodeDescriptor? = vision.descriptor,
    key: Any? = null,
) {

    val styles = useMemo(vision, key) {
        if (vision is SolidReference) {
            (vision.styles + vision.prototype.styles).distinct()
        } else {
            vision.styles
        }
    }
    flexColumn {
        ringIsland("Properties") {
            propertyEditor(
                ownProperties = vision.ownProperties,
                allProperties = vision.allProperties(),
                updateFlow = vision.propertyChanges,
                descriptor = descriptor,
                key = key
            )
        }

        if (styles.isNotEmpty()) {
            ringIsland {
                ringIslandHeader {
                    attrs {
                        border = true
                    }
                    +"Styles"
                }
                ringIslandContent {
                    if (styles.size == 1) {
                        val styleName = styles.first()
                        p{
                            +styleName
                        }
                        val style = vision.getStyle(styleName)
                        if (style != null) {
                            ringTab(styleName, id = styleName) {
                                metaViewer(style)
                            }
                        }
                    } else {
                        ringSmartTabs {
                            styles.forEach { styleName ->
                                val style = vision.getStyle(styleName)
                                if (style != null) {
                                    ringTab(styleName, id = styleName) {
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
}

public fun Element.ringPropertyEditor(
    item: Vision,
    descriptor: NodeDescriptor? = item.descriptor,
): Unit = render(this) {
    ringPropertyEditor(item, descriptor = descriptor)
}