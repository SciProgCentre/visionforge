package hep.dataforge.vis.js.editor

import hep.dataforge.js.jsObject
import hep.dataforge.meta.DynamicMeta
import hep.dataforge.meta.Meta
import hep.dataforge.meta.toJson
import hep.dataforge.meta.update
import hep.dataforge.names.Name
import hep.dataforge.names.isEmpty
import hep.dataforge.vis.common.VisualObject
import hep.dataforge.vis.common.findStyle
import kotlinx.html.dom.append
import kotlinx.html.js.*
import org.w3c.dom.Element
import kotlin.collections.set
import kotlin.dom.clear

//FIXME something rotten in JS-Meta converter
fun Meta.toDynamic() = JSON.parse<dynamic>(toJson().toString())

//TODO add node descriptor instead of configuring property selector
fun Element.displayPropertyEditor(
    name: Name,
    item: VisualObject,
    propertySelector: (VisualObject) -> Meta = { it.config }
) {
    clear()

    append {
        card("Properties") {
            if (!name.isEmpty()) {
                nav {
                    attributes["aria-label"] = "breadcrumb"
                    ol("breadcrumb") {
                        name.tokens.forEach { token ->
                            li("breadcrumb-item") {
                                +token.toString()
                            }
                        }
                    }
                }
            }
            val dMeta: dynamic = propertySelector(item).toDynamic()
            val options: JSONEditorOptions = jsObject {
                mode = "form"
                onChangeJSON = { item.config.update(DynamicMeta(it.asDynamic())) }
            }
            JSONEditor(div(), options, dMeta)
        }

        val styles = item.styles
        if (styles.isNotEmpty()) {
            card("Styles") {
                item.styles.forEach { style ->
                    val styleMeta = item.findStyle(style)
                    h4("container") { +style }
                    if (styleMeta != null) {
                        div("container").apply {
                            val options: JSONEditorOptions = jsObject {
                                mode = "view"
                            }
                            JSONEditor(
                                this,
                                options,
                                styleMeta.toDynamic()
                            )
                        }
                    }
                }
            }
        }
    }
}