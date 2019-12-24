package hep.dataforge.js

import hep.dataforge.names.NameToken
import hep.dataforge.vis.common.VisualGroup
import hep.dataforge.vis.common.VisualObject
import hep.dataforge.vis.common.isEmpty
import hep.dataforge.vis.js.editor.card
import kotlinx.html.TagConsumer
import kotlinx.html.dom.append
import kotlinx.html.js.*
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLSpanElement
import kotlin.dom.clear

fun Element.objectTree(
    token: NameToken,
    obj: VisualObject,
    clickCallback: (VisualObject) -> Unit = {}
) {
    clear()
    append {
        card("Object tree") {
            subTree(token, obj, clickCallback)
        }
    }
}

private fun TagConsumer<HTMLElement>.subTree(
    token: NameToken,
    obj: VisualObject,
    clickCallback: (VisualObject) -> Unit
) {

    if (obj is VisualGroup && !obj.isEmpty) {
        lateinit var toggle: HTMLSpanElement
        div("d-inline-block text-truncate") {
            toggle = span("objTree-caret")
            label("objTree-label") {
                +token.toString()
                onClickFunction = { clickCallback(obj) }
            }
        }
        val subtree = ul("objTree-subtree")
        toggle.onclick = {
            toggle.classList.toggle("objTree-caret-down")
            subtree.apply {
                if (toggle.classList.contains("objTree-caret-down")) {
                    obj.children.entries
                        .filter { !it.key.toString().startsWith("@") }
                        .sortedBy { (it.value as? VisualGroup)?.isEmpty ?: true }
                        .forEach { (token, child) ->
                            append {
                                li().apply {
                                    subTree(token, child, clickCallback)
                                }
                            }
                        }
                } else {
                    this.clear()
                }
            }
            //jQuery(subtree).asDynamic().collapse("toggle")
        }
    } else {
        div("d-inline-block text-truncate") {
            span("objTree-leaf")
            label("objTree-label") {
                +token.toString()
                onClickFunction = { clickCallback(obj) }
            }
        }
    }
}

