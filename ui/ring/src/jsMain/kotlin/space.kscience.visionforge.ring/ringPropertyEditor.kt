package space.kscience.visionforge.ring

import org.w3c.dom.Element
import react.RBuilder
import react.dom.p
import ringui.Island
import ringui.SmartTabs
import ringui.Tab
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.dataforge.meta.get
import space.kscience.visionforge.Vision
import space.kscience.visionforge.getStyle
import space.kscience.visionforge.react.*
import space.kscience.visionforge.root
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
            child(PropertyEditor) {
                attrs {
                    this.key = key?.toString()
                    this.meta = vision.properties.root()
                    this.updates = vision.properties.changes
                    this.descriptor = descriptor
                    this.scope = vision.manager?.context ?: error("Orphan vision could not be observed")
                    this.getPropertyState = {name->
                        if(vision.properties.own?.get(name)!= null){
                            EditorPropertyState.Defined
                        } else if(vision.properties.root()[name] != null){
                            // TODO differentiate
                            EditorPropertyState.Default()
                        } else {
                            EditorPropertyState.Undefined
                        }
                    }
                }
            }
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