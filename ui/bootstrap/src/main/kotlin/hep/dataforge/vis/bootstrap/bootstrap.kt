package hep.dataforge.vis.bootstrap

import hep.dataforge.names.Name
import hep.dataforge.names.NameToken
import kotlinx.html.*
import kotlinx.html.js.div
import kotlinx.html.js.onClickFunction
import org.w3c.dom.HTMLElement
import react.RBuilder
import react.ReactElement
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
    div("card w-100") {
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

fun RBuilder.accordion(id: String, elements: List<Pair<String, RDOMBuilder<DIV>.() -> Unit>>): ReactElement {
    return div("container-fluid") {
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

fun RBuilder.namecrumbs(name: Name?, rootTitle: String, link: (Name) -> Unit) {
    div("container-fluid p-0") {
        nav {
            attrs {
                attributes["aria-label"] = "breadcrumb"
            }
            ol("breadcrumb") {
                li("breadcrumb-item") {
                    button(classes = "btn btn-link p-0") {
                        +rootTitle
                        attrs {
                            onClickFunction = {
                                link(Name.EMPTY)
                            }
                        }
                    }
                }
                if (name != null) {
                    val tokens = ArrayList<NameToken>(name.length)
                    name.tokens.forEach { token ->
                        tokens.add(token)
                        val fullName = Name(tokens.toList())
                        li("breadcrumb-item") {
                            button(classes = "btn btn-link p-0") {
                                +token.toString()
                                attrs {
                                    onClickFunction = {
                                        console.log("Selected = $fullName")
                                        link(fullName)
                                    }
                                }
                            }
                        }
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

fun RBuilder.accordion(id: String, builder: RAccordionBuilder.() -> Unit): ReactElement {
    val list = ArrayList<Pair<String, RDOMBuilder<DIV>.() -> Unit>>().apply(builder)
    return accordion(id, list)
}

fun joinStyles(vararg styles: String?) = styles.joinToString(separator = " ") { it ?: "" }

inline fun RBuilder.flexColumn(classes: String? = null, block: RDOMBuilder<DIV>.() -> Unit) =
    div(joinStyles(classes, "d-flex flex-column"), block)

inline fun RBuilder.flexRow(classes: String? = null, block: RDOMBuilder<DIV>.() -> Unit) =
    div(joinStyles(classes, "d-flex flex-row"), block)

enum class ContainerSize(val suffix: String) {
    DEFAULT(""),
    SM("-sm"),
    MD("-md"),
    LG("-lg"),
    XL("-xl"),
    FLUID("-fluid")
}

inline fun RBuilder.container(
    classes: String? = null,
    size: ContainerSize = ContainerSize.FLUID,
    block: RDOMBuilder<DIV>.() -> Unit
): ReactElement = div(joinStyles(classes, "container${size.suffix}"), block)


enum class GridMaxSize(val suffix: String) {
    NONE(""),
    SM("-sm"),
    MD("-md"),
    LG("-lg"),
    XL("-xl")
}

inline fun RBuilder.gridColumn(
    weight: Int? = null,
    classes: String? = null,
    maxSize: GridMaxSize = GridMaxSize.NONE,
    block: RDOMBuilder<DIV>.() -> Unit
): ReactElement {
    val weightSuffix = weight?.let { "-$it" } ?: ""
    return div(joinStyles(classes, "col${maxSize.suffix}$weightSuffix"), block)
}

inline fun RBuilder.gridRow(
    classes: String? = null,
    block: RDOMBuilder<DIV>.() -> Unit
): ReactElement {
    return div(joinStyles(classes, "row"), block)
}