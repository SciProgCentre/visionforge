package hep.dataforge.vis.editor

import hep.dataforge.meta.Meta
import hep.dataforge.meta.MetaBuilder
import hep.dataforge.meta.descriptors.NodeDescriptor
import hep.dataforge.names.Name
import hep.dataforge.names.isEmpty
import hep.dataforge.vis.VisualObject
import org.w3c.dom.Element
import react.RBuilder
import react.ReactElement
import react.dom.li
import react.dom.nav
import react.dom.ol
import react.dom.render
import kotlin.collections.set

////FIXME something rotten in JS-Meta converter
//fun Meta.toDynamic() = JSON.parse<dynamic>(toJson().toString())
//
////TODO add node descriptor instead of configuring property selector
//fun Element.displayPropertyEditor(
//    name: Name,
//    item: VisualObject,
//    propertySelector: (VisualObject) -> Meta = { it.config }
//) {
//    clear()
//
//    append {
//        card("Properties") {
//            if (!name.isEmpty()) {
//                nav {
//                    attributes["aria-label"] = "breadcrumb"
//                    ol("breadcrumb") {
//                        name.tokens.forEach { token ->
//                            li("breadcrumb-item") {
//                                +token.toString()
//                            }
//                        }
//                    }
//                }
//            }
//            val dMeta: dynamic = propertySelector(item).toDynamic()
//            val options: JSONEditorOptions = jsObject {
//                mode = "form"
//                onChangeJSON = { item.config.update(DynamicMeta(it.asDynamic())) }
//            }
//            JSONEditor(div(), options, dMeta)
//        }
//
//        val styles = item.styles
//        if (styles.isNotEmpty()) {
//            card("Styles") {
//                item.styles.forEach { style ->
//                    val styleMeta = item.findStyle(style)
//                    h4("container") { +style }
//                    if (styleMeta != null) {
//                        div("container").apply {
//                            val options: JSONEditorOptions = jsObject {
//                                mode = "view"
//                            }
//                            JSONEditor(
//                                this,
//                                options,
//                                styleMeta.toDynamic()
//                            )
//                        }
//                    }
//                }
//            }
//        }
//    }
//}

fun RBuilder.visualPropertyEditor(
    path: Name,
    item: VisualObject,
    descriptor: NodeDescriptor? = item.descriptor,
    title: String = "Properties",
    default: MetaBuilder.() -> Unit = {}
): ReactElement = card(title) {
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
    configEditor(item.config, descriptor, Meta(default))
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