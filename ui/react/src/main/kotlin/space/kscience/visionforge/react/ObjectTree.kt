package space.kscience.visionforge.react

import kotlinx.html.js.onClickFunction
import org.w3c.dom.events.Event
import react.*
import react.dom.attrs
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.lastOrNull
import space.kscience.dataforge.names.plus
import space.kscience.dataforge.names.startsWith
import space.kscience.visionforge.Vision
import space.kscience.visionforge.VisionGroup
import space.kscience.visionforge.isEmpty
import styled.*

public external interface ObjectTreeProps : RProps {
    public var name: Name
    public var selected: Name?
    public var obj: Vision
    public var clickCallback: (Name) -> Unit
}

private fun RBuilder.objectTree(props: ObjectTreeProps): Unit {
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
public val ObjectTree: FunctionalComponent<ObjectTreeProps> = functionalComponent("ObjectTree") { props ->
    objectTree(props)
}

public fun RBuilder.objectTree(
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

