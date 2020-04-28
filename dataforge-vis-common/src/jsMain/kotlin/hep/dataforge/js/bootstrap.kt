package hep.dataforge.js

import kotlinx.html.*
import kotlinx.html.js.div
import org.w3c.dom.HTMLElement
import react.RBuilder
import react.dom.*

inline fun TagConsumer<HTMLElement>.card(title: String, crossinline block: TagConsumer<HTMLElement>.() -> Unit) {
    div("card w-100") {
        div("card-body") {
            h3(classes = "card-title") { +title }
            block()
        }
    }
}

inline fun RBuilder.card(title: String, crossinline block: RBuilder.() -> Unit) {
    div("card w-100 h-100") {
        div("card-body") {
            h3(classes = "card-title") {
                +title
            }
            block()
        }
    }
}


fun TagConsumer<HTMLElement>.accordion(id: String, elements: List<Pair<String, DIV.() -> Unit>>) {
    div("container-fluid") {
        div("accordion") {
            this.id = id
            elements.forEachIndexed { index, (title, builder) ->
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


typealias AccordionBuilder = MutableList<Pair<String, DIV.() -> Unit>>

fun AccordionBuilder.entry(title: String, builder: DIV.() -> Unit) {
    add(title to builder)
}

fun TagConsumer<HTMLElement>.accordion(id: String, builder: AccordionBuilder.() -> Unit) {
    val list = ArrayList<Pair<String, DIV.() -> Unit>>().apply(builder)
    accordion(id, list)
}

fun RBuilder.accordion(id: String, elements: List<Pair<String, RDOMBuilder<DIV>.() -> Unit>>) {
    div("container-fluid") {
        div("accordion") {
            attrs {
                this.id = id
            }
            elements.forEachIndexed { index, (title, builder) ->
                val headerID = "${id}-${index}-heading"
                val collapseID = "${id}-${index}-collapse"
                div("card p-0 m-0") {
                    div("card-header") {
                        attrs {
                            this.id = headerID
                        }
                        h5("mb-0") {
                            button(classes = "btn btn-link collapsed", type = ButtonType.button) {
                                attrs {
                                    attributes["data-toggle"] = "collapse"
                                    attributes["data-target"] = "#$collapseID"
                                    attributes["aria-expanded"] = "false"
                                    attributes["aria-controls"] = collapseID
                                }
                                +title
                            }
                        }
                    }
                    div("collapse") {
                        attrs {
                            this.id = collapseID
                            attributes["aria-labelledby"] = headerID
                            attributes["data-parent"] = "#$id"
                        }
                        div("card-body", block = builder)
                    }
                }
            }
        }
    }
}

typealias RAccordionBuilder = MutableList<Pair<String, RDOMBuilder<DIV>.() -> Unit>>

fun RAccordionBuilder.entry(title: String, builder: RDOMBuilder<DIV>.() -> Unit) {
    add(title to builder)
}

fun RBuilder.accordion(id: String, builder: RAccordionBuilder.() -> Unit) {
    val list = ArrayList<Pair<String, RDOMBuilder<DIV>.() -> Unit>>().apply(builder)
    accordion(id, list)
}