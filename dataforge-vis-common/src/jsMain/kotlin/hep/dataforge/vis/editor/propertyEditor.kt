package hep.dataforge.vis.editor

import hep.dataforge.js.card
import hep.dataforge.meta.Meta
import hep.dataforge.meta.MetaBuilder
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
    title: String = "Properties",
    default: MetaBuilder.() -> Unit = {}
) {
    card(title) {
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
        configEditor(item, descriptor, Meta(default))
    }
}

fun Element.visualPropertyEditor(
    path: Name,
    item: VisualObject,
    descriptor: NodeDescriptor? = item.descriptor,
    title: String = "Properties",
    default: MetaBuilder.() -> Unit = {}
) = render(this) {
    visualPropertyEditor(path, item, descriptor, title, default)
}