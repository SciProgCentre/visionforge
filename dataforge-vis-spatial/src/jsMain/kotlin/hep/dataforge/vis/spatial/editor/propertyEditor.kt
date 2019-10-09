package hep.dataforge.vis.spatial.editor

import hep.dataforge.io.toJson
import hep.dataforge.meta.*
import hep.dataforge.names.toName
import hep.dataforge.vis.common.VisualObject
import hep.dataforge.vis.common.findStyle
import hep.dataforge.vis.hmr.jsObject
import hep.dataforge.vis.spatial.Material3D.Companion.COLOR_KEY
import hep.dataforge.vis.spatial.Material3D.Companion.OPACITY_KEY
import hep.dataforge.vis.spatial.VisualObject3D.Companion.VISIBLE_KEY
import hep.dataforge.vis.spatial.color
import hep.dataforge.vis.spatial.opacity
import hep.dataforge.vis.spatial.prototype
import hep.dataforge.vis.spatial.visible
import kotlinx.html.dom.append
import kotlinx.html.js.div
import kotlinx.html.js.h3
import kotlinx.html.js.h4
import org.w3c.dom.Element
import kotlin.dom.clear

fun Meta.toDynamic() = JSON.parse<dynamic>(toJson().toString())


fun Element.propertyEditor(item: VisualObject?, name: String?) {
    clear()
    if (item != null) {
        append {
            div("card") {
                div("card-body") {
                    h3(classes = "card-title") { +"Properties" }
                }.apply {
                    val config = (item.properties ?: item.prototype?.properties) ?: EmptyMeta
                    val metaToEdit = config.builder().apply {
                        VISIBLE_KEY to (item.visible ?: true)
                        COLOR_KEY to (item.color ?: "#ffffff")
                        OPACITY_KEY to (item.opacity ?: 1.0)
                    }
                    //FIXME something rotten in JS-Meta converter
                    val dMeta: dynamic = metaToEdit.toDynamic()
                    //jsObject.material.color != null
                    val options: JSONEditorOptions = jsObject{
                        mode = "form"
                        onChangeJSON = { item.config.update(DynamicMeta(it.asDynamic())) }
                    }
                    JSONEditor(this, options, dMeta)
                }
            }


            div("card") {
                div("card-body") {
                    h3(classes = "card-title") { +"Styles" }
                }
                item.styles.forEach { style ->
                    val styleMeta = item.findStyle(style)
                    h4 { +style.toString() }
                    if (styleMeta != null) {
                        div("container").apply {
                            val options: JSONEditorOptions = jsObject{
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
