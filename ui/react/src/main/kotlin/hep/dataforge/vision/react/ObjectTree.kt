package hep.dataforge.vision.react

import hep.dataforge.names.Name
import hep.dataforge.names.lastOrNull
import hep.dataforge.names.plus
import hep.dataforge.names.startsWith
import hep.dataforge.vision.Vision
import hep.dataforge.vision.VisionGroup
import hep.dataforge.vision.isEmpty
import kotlinx.html.js.onClickFunction
import org.w3c.dom.events.Event
import react.*
import styled.*

public external interface ObjectTreeProps : RProps {
    var name: Name
    var selected: Name?
    var obj: Vision
    var clickCallback: (Name) -> Unit
}

private fun RFBuilder.objectTree(props: ObjectTreeProps): Unit {
    var expanded: Boolean by useState { props.selected?.startsWith(props.name) ?: false }

    val onClick: (Event) -> Unit = {
        expanded = !expanded
    }

    fun RBuilder.treeLabel(text: String) {
        styledButton {
            css {
                //classes = mutableListOf("btn", "btn-link", "align-middle", "text-truncate", "p-0")
                +TreeStyles.treeLabel
                +TreeStyles.linkButton
                if (props.name == props.selected) {
                    +TreeStyles.treeLabelSelected
                }
            }
            +text
            attrs {
                onClickFunction = { props.clickCallback(props.name) }
            }
        }
    }

    val token = props.name.lastOrNull()?.toString() ?: "World"
    val obj = props.obj

    //display as node if any child is visible
    if (obj is VisionGroup) {
        styledDiv {
            css {
                +TreeStyles.treeLeaf
            }
            if (obj.children.any { !it.key.body.startsWith("@") }) {
                styledSpan {
                    css {
                        +TreeStyles.treeCaret
                        if (expanded) {
                            +TreeStyles.treeCaredDown
                        }
                    }
                    attrs {
                        onClickFunction = onClick
                    }
                }
            }
            treeLabel(token)
        }
        if (expanded) {
            styledUl {
                css {
                    +TreeStyles.tree
                }
                obj.children.entries
                    .filter { !it.key.toString().startsWith("@") } // ignore statics and other hidden children
                    .sortedBy { (it.value as? VisionGroup)?.isEmpty ?: true } // ignore empty groups
                    .forEach { (childToken, child) ->
                        styledLi {
                            css {
                                +TreeStyles.treeItem
                            }
                            child(ObjectTree) {
                                attrs {
                                    this.name = props.name + childToken
                                    this.obj = child
                                    this.selected = props.selected
                                    this.clickCallback = props.clickCallback
                                }
                            }
                        }
                    }
            }
        }
    } else {
        styledDiv {
            css {
                +TreeStyles.treeLeaf
            }
            treeLabel(token)
        }
    }
}

@JsExport
val ObjectTree: FunctionalComponent<ObjectTreeProps> = component { props ->
    objectTree(props)
}

fun RBuilder.objectTree(
    vision: Vision,
    selected: Name? = null,
    clickCallback: (Name) -> Unit = {}
) {
    child(ObjectTree) {
        attrs {
            this.name = Name.EMPTY
            this.obj = vision
            this.selected = selected
            this.clickCallback = clickCallback
        }
    }
}

