package hep.dataforge.vis.spatial.editor

import hep.dataforge.io.toJson
import hep.dataforge.js.jsObject
import hep.dataforge.meta.*
import hep.dataforge.vis.common.VisualObject
import hep.dataforge.vis.common.findStyle
import hep.dataforge.vis.spatial.Material3D.Companion.COLOR_KEY
import hep.dataforge.vis.spatial.Material3D.Companion.OPACITY_KEY
import hep.dataforge.vis.spatial.VisualObject3D.Companion.VISIBLE_KEY
import hep.dataforge.vis.spatial.color
import hep.dataforge.vis.spatial.opacity
import hep.dataforge.vis.spatial.prototype
import hep.dataforge.vis.spatial.visible
import kotlinx.html.dom.append
import kotlinx.html.js.div
import kotlinx.html.js.h4
import org.w3c.dom.Element
import kotlin.dom.clear

//FIXME something rotten in JS-Meta converter
fun Meta.toDynamic() = JSON.parse<dynamic>(toJson().toString())


fun Element.propertyEditor(item: VisualObject?) {
    clear()
    if (item != null) {
        append {
            card("Properties") {
                val config = (item.properties ?: item.prototype?.properties) ?: EmptyMeta
                val metaToEdit = config.builder().apply {
                    VISIBLE_KEY to (item.visible ?: true)
                    COLOR_KEY to (item.color ?: "#ffffff")
                    OPACITY_KEY to (item.opacity ?: 1.0)
                }
                val dMeta: dynamic = metaToEdit.toDynamic()
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
                        h4("container") { +style.toString() }
                        if (styleMeta != null) {
                            div("container").apply {
                                val options: JSONEditorOptions = jsObject {
                                    mode = "view"
                                }
                                JSONEditor(this, options, styleMeta.toDynamic())
                            }
                        }
                    }
                }
            }
        }
    }
}
