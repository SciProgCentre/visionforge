package hep.dataforge.vis.js.editor

import kotlinx.html.*
import kotlinx.html.js.div
import org.w3c.dom.HTMLElement

inline fun TagConsumer<HTMLElement>.card(title: String, crossinline block: TagConsumer<HTMLElement>.() -> Unit) {
    div("card w-100") {
        div("card-body") {
            h3(classes = "card-title") { +title }
            block()
        }
    }
}

fun TagConsumer<HTMLElement>.accordion(id: String, elements: Map<String, DIV.() -> Unit>) {
    div("container-fluid") {
        div("accordion") {
            this.id = id
            elements.entries.forEachIndexed { index, (title, builder) ->
                val headerID = "${id}-${index}-heading"
                val collapseID = "${id}-${index}-collapse"
                div("card") {
                    div("card-header") {
                        this.id = headerID
                        h5("mb-0") {
                            button(classes = "btn btn-link collapsed", type = ButtonType.button) {
                                attributes["data-toggle"] = "collapse"
                                attributes["data-target"] = "#$collapseID"
                                attributes["aria-expanded"] = "false"
                                attributes["aria-controls"] = collapseID
                                +title
                            }
                        }
                    }
                    div("collapse") {
                        this.id = collapseID
                        attributes["aria-labelledby"] = headerID
                        attributes["data-parent"] = "#$id"
                        div("card-body", block = builder)
                    }
                }
            }
        }
    }
}

class AccordionBuilder {
    private val map = HashMap<String, DIV.() -> Unit>()
    fun entry(title: String, block: DIV.() -> Unit) {
        map[title] = block
    }

    fun build(consumer: TagConsumer<HTMLElement>, id: String) {
        consumer.accordion(id, map)
    }
}

fun TagConsumer<HTMLElement>.accordion(id: String, block: AccordionBuilder.() -> Unit) {
    AccordionBuilder().apply(block).build(this, id)
}