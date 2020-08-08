package hep.dataforge.vision.material

import hep.dataforge.vision.react.component
import hep.dataforge.vision.react.state
import kotlinx.html.DIV
import materialui.components.card.card
import materialui.components.cardcontent.cardContent
import materialui.components.cardheader.cardHeader
import materialui.components.container.container
import materialui.components.container.enums.ContainerMaxWidth
import materialui.components.expansionpanel.expansionPanel
import materialui.components.expansionpaneldetails.expansionPanelDetails
import materialui.components.expansionpanelsummary.expansionPanelSummary
import materialui.components.grid.GridElementBuilder
import materialui.components.grid.enums.GridDirection
import materialui.components.grid.enums.GridStyle
import materialui.components.grid.grid
import materialui.components.paper.paper
import materialui.components.typography.typographyH5
import react.RBuilder
import react.RProps
import react.child
import react.dom.RDOMBuilder


fun accordionComponent(elements: List<Pair<String, RDOMBuilder<DIV>.() -> Unit>>) =
    component<RProps> {
        val expandedIndex: Int? by state { null }

        container {
            attrs {
                maxWidth = ContainerMaxWidth.`false`
            }
            elements.forEachIndexed { index, (header, body) ->
                expansionPanel {
                    attrs {
                        expanded = index == expandedIndex
                    }
                    expansionPanelSummary {
                        typographyH5 {
                            +header
                        }
                    }
                    expansionPanelDetails {
                        this.body()
                    }
                }
            }
        }
    }

typealias RAccordionBuilder = MutableList<Pair<String, RDOMBuilder<DIV>.() -> Unit>>

fun RAccordionBuilder.entry(title: String, builder: RDOMBuilder<DIV>.() -> Unit) {
    add(title to builder)
}

fun RBuilder.accordion(builder: RAccordionBuilder.() -> Unit) {
    val list: List<Pair<String, RDOMBuilder<DIV>.() -> Unit>> =
        ArrayList<Pair<String, RDOMBuilder<DIV>.() -> Unit>>().apply(builder)
    child(accordionComponent(list))
}

fun RBuilder.materialCard(title: String, block: RBuilder.() -> Unit) {
    card {
        cardHeader {
            attrs {
                this.title = typographyH5 {
                    +title
                }
            }
        }
        cardContent {
            paper {
                this.block()
            }
        }
    }
}

fun RBuilder.column(vararg classMap: Pair<GridStyle, String>, block: GridElementBuilder<DIV>.() -> Unit) =
    grid(*classMap) {
        attrs {
            container = true
            direction = GridDirection.column
        }
        block()
    }

fun RBuilder.row(vararg classMap: Pair<GridStyle, String>, block: GridElementBuilder<DIV>.() -> Unit) =
    grid(*classMap) {
        attrs {
            container = true
            direction = GridDirection.row
        }
        block()
    }
