package hep.dataforge.vis.js.editor

import hep.dataforge.io.toJson
import hep.dataforge.js.jsObject
import hep.dataforge.meta.DynamicMeta
import hep.dataforge.meta.Meta
import hep.dataforge.meta.update
import hep.dataforge.vis.common.VisualObject
import hep.dataforge.vis.common.findStyle
import kotlinx.html.dom.append
import kotlinx.html.js.div
import kotlinx.html.js.h4
import org.w3c.dom.Element
import kotlin.dom.clear

//FIXME something rotten in JS-Meta converter
fun Meta.toDynamic() = JSON.parse<dynamic>(toJson().toString())

//TODO add node descriptor instead of configuring property selector
fun Element.propertyEditor(item: VisualObject?, propertySelector: (VisualObject) -> Meta = { it.config }) {
    clear()
    if (item != null) {
        append {
            card("Properties") {
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
}