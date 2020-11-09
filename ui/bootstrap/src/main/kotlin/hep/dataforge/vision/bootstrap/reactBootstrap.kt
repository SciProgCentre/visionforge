package hep.dataforge.vision.bootstrap

import hep.dataforge.names.Name
import hep.dataforge.names.NameToken
import hep.dataforge.names.length
import kotlinx.html.ButtonType
import kotlinx.html.DIV
import kotlinx.html.id
import kotlinx.html.js.onClickFunction
import react.RBuilder
import react.ReactElement
import react.dom.*
import styled.StyledDOMBuilder
import styled.css
import styled.styledDiv
import styled.styledNav


public inline fun RBuilder.card(title: String, crossinline block: StyledDOMBuilder<DIV>.() -> Unit): ReactElement =
    styledDiv {
        css {
            +"card"
            +"w-100"
        }
        styledDiv {
            css {
                +"card-body"
            }
            h3(classes = "card-title") {
                +title
            }
            block()
        }
    }

public fun RBuilder.accordion(
    id: String,
    elements: List<Pair<String, StyledDOMBuilder<DIV>.() -> Unit>>,
): ReactElement = styledDiv {
    css {
        +"accordion"
        //+"p-1"
    }
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
                styledDiv {
                    css {
                        +"card-body"
                    }
                    builder()
                }
            }
        }
    }
}


public fun RBuilder.nameCrumbs(name: Name?, rootTitle: String, link: (Name) -> Unit): ReactElement = styledNav {
    css {
        +"p-0"
    }
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

public typealias RSectionsBuilder = MutableList<Pair<String, StyledDOMBuilder<DIV>.() -> Unit>>

public fun RSectionsBuilder.entry(title: String, builder: StyledDOMBuilder<DIV>.() -> Unit) {
    add(title to builder)
}

public fun RBuilder.accordion(id: String, builder: RSectionsBuilder.() -> Unit): ReactElement {
    val list = ArrayList<Pair<String, StyledDOMBuilder<DIV>.() -> Unit>>().apply(builder)
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
    block: StyledDOMBuilder<DIV>.() -> Unit,
): ReactElement = styledDiv {
    css {
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
    block: StyledDOMBuilder<DIV>.() -> Unit,
): ReactElement = styledDiv {
    val weightSuffix = weight?.let { "-$it" } ?: ""
    css {
        classes.add("col${maxSize.suffix}$weightSuffix")
    }
    block()
}

public inline fun RBuilder.gridRow(
    block: StyledDOMBuilder<DIV>.() -> Unit,
): ReactElement = styledDiv {
    css {
        classes.add("row")
    }
    block()
}