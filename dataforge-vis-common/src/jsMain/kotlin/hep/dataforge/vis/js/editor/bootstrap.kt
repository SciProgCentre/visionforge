package hep.dataforge.vis.js.editor

import kotlinx.html.TagConsumer
import kotlinx.html.js.div
import kotlinx.html.js.h3
import org.w3c.dom.HTMLElement

inline fun TagConsumer<HTMLElement>.card(title: String, crossinline block: TagConsumer<HTMLElement>.() -> Unit) {
    div("card w-100") {
        div("card-body") {
            h3(classes = "card-title") { +title }
            block()
        }
    }
}