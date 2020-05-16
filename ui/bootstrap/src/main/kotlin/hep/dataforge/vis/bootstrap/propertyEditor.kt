package hep.dataforge.vis.bootstrap

import hep.dataforge.meta.Meta
import hep.dataforge.meta.descriptors.NodeDescriptor
import hep.dataforge.names.Name
import hep.dataforge.names.isEmpty
import hep.dataforge.vis.VisualObject
import org.w3c.dom.Element
import react.RBuilder
import react.dom.li
import react.dom.nav
import react.dom.ol
import react.dom.render
import kotlin.collections.set

fun RBuilder.visualPropertyEditor(
    path: Name,
    item: VisualObject,
    descriptor: NodeDescriptor? = item.descriptor,
    default: Meta? = null
) {
    card("Properties") {
        if (!path.isEmpty()) {
            nav {
                attrs {
                    attributes["aria-label"] = "breadcrumb"
                }
                ol("breadcrumb") {
                    path.tokens.forEach { token ->
                        li("breadcrumb-item") {
                            +token.toString()
                        }
                    }
                }
            }
        }
        configEditor(item, descriptor, default)
    }
}

fun Element.visualPropertyEditor(
    path: Name,
    item: VisualObject,
    descriptor: NodeDescriptor? = item.descriptor,
    default: Meta? = null
) = render(this) {
    this.visualPropertyEditor(path, item, descriptor, default)
}