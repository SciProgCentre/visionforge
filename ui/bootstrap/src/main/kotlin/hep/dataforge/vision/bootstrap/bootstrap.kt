package hep.dataforge.vision.bootstrap

import hep.dataforge.names.Name
import hep.dataforge.names.NameToken
import hep.dataforge.names.length
import hep.dataforge.vision.Vision
import hep.dataforge.vision.react.ObjectTree
import kotlinx.html.*
import kotlinx.html.js.div
import kotlinx.html.js.onClickFunction
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import react.RBuilder
import react.ReactElement
import react.child
import react.dom.*
import styled.StyledDOMBuilder
import styled.css
import styled.styledDiv

public inline fun TagConsumer<HTMLElement>.card(title: String, crossinline block: TagConsumer<HTMLElement>.() -> Unit) {
    div("card w-100") {
        div("card-body") {
            h3(classes = "card-title") { +title }
            block()
        }
    }
}

public inline fun RBuilder.card(title: String, classes: String? = null, crossinline block: RBuilder.() -> Unit) {
    div("card w-100 $classes") {
        div("card-body") {
            h3(classes = "card-title") {
                +title
            }
            block()
        }
    }
}

public fun TagConsumer<HTMLElement>.accordion(id: String, elements: List<Pair<String, DIV.() -> Unit>>) {
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

public typealias AccordionBuilder = MutableList<Pair<String, DIV.() -> Unit>>

public fun AccordionBuilder.entry(title: String, builder: DIV.() -> Unit) {
    add(title to builder)
}

public fun TagConsumer<HTMLElement>.accordion(id: String, builder: AccordionBuilder.() -> Unit) {
    val list = ArrayList<Pair<String, DIV.() -> Unit>>().apply(builder)
    accordion(id, list)
}

public fun RBuilder.accordion(id: String, elements: List<Pair<String, RDOMBuilder<DIV>.() -> Unit>>): ReactElement {
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

public fun RBuilder.namecrumbs(name: Name?, rootTitle: String, link: (Name) -> Unit) {
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

public typealias RAccordionBuilder = MutableList<Pair<String, RDOMBuilder<DIV>.() -> Unit>>

public fun RAccordionBuilder.entry(title: String, builder: RDOMBuilder<DIV>.() -> Unit) {
    add(title to builder)
}

public fun RBuilder.accordion(id: String, builder: RAccordionBuilder.() -> Unit): ReactElement {
    val list = ArrayList<Pair<String, RDOMBuilder<DIV>.() -> Unit>>().apply(builder)
    return accordion(id, list)
}

public enum class ContainerSize(public val suffix: String) {
    DEFAULT(""),
    SM("-sm"),
    MD("-md"),
    LG("-lg"),
    XL("-xl"),
    FLUID("-fluid")
}

public inline fun RBuilder.container(
    size: ContainerSize = ContainerSize.FLUID,
    block: StyledDOMBuilder<DIV>.() -> Unit
): ReactElement = styledDiv{
    css{
        classes.add("container${size.suffix}")
    }
    block()
}


public enum class GridMaxSize(public val suffix: String) {
    NONE(""),
    SM("-sm"),
    MD("-md"),
    LG("-lg"),
    XL("-xl")
}

public inline fun RBuilder.gridColumn(
    weight: Int? = null,
    maxSize: GridMaxSize = GridMaxSize.NONE,
    block: StyledDOMBuilder<DIV>.() -> Unit
): ReactElement = styledDiv {
    val weightSuffix = weight?.let { "-$it" } ?: ""
    css {
        classes.add("col${maxSize.suffix}$weightSuffix")
    }
    block()
}

public inline fun RBuilder.gridRow(
    block: StyledDOMBuilder<DIV>.() -> Unit
): ReactElement = styledDiv{
    css{
        classes.add("row")
    }
    block()
}

public fun Element.renderObjectTree(
    vision: Vision,
    clickCallback: (Name) -> Unit = {}
): Unit = render(this) {
    card("Object tree") {
        child(ObjectTree) {
            attrs {
                this.name = Name.EMPTY
                this.obj = vision
                this.selected = null
                this.clickCallback = clickCallback
            }
        }
    }
}