package hep.dataforge.vis.editor

import hep.dataforge.js.card
import hep.dataforge.names.Name
import hep.dataforge.names.plus
import hep.dataforge.names.startsWith
import hep.dataforge.vis.VisualGroup
import hep.dataforge.vis.VisualObject
import hep.dataforge.vis.isEmpty
import kotlinx.html.classes
import kotlinx.html.js.onClickFunction
import org.w3c.dom.Element
import org.w3c.dom.events.Event
import react.*
import react.dom.*

interface ObjectTreeProps : RProps {
    var name: Name
    var selected: Name?
    var obj: VisualObject
    var clickCallback: (Name) -> Unit
}

interface TreeState : RState {
    var expanded: Boolean
}

class ObjectTree : RComponent<ObjectTreeProps, TreeState>() {

    override fun TreeState.init(props: ObjectTreeProps) {
        expanded = props.selected?.startsWith(props.name) ?: false
    }


    private val onClick: (Event) -> Unit = {
        setState {
            expanded = !expanded
        }
    }

    private fun RBuilder.treeLabel(text: String) {
        a("#", classes = "tree-label") {
            +text
            attrs {
                if (props.name == props.selected) {
                    classes += "tree-label-selected"
                }
                onClickFunction = { props.clickCallback(props.name) }
            }
        }
    }

    override fun RBuilder.render() {
        val token = props.name.last()?.toString() ?: "World"
        val obj = props.obj

        //display as node if any child is visible
        if (obj is VisualGroup) {
            div("d-inline-block text-truncate") {
                if (obj.children.any { !it.key.body.startsWith("@") }) {
                    span("tree-caret") {
                        attrs {
                            if (state.expanded) {
                                classes += "tree-caret-down"
                            }
                            onClickFunction = onClick
                        }
                    }
                }
                treeLabel(token)
            }
            if (state.expanded) {
                ul("tree") {
                    obj.children.entries
                        .filter { !it.key.toString().startsWith("@") } // ignore statics and other hidden children
                        .sortedBy { (it.value as? VisualGroup)?.isEmpty ?: true }
                        .forEach { (childToken, child) ->
                            li("tree-item") {
                                child(ObjectTree::class) {
                                    attrs {
                                        name = props.name + childToken
                                        this.obj = child
                                        this.selected = props.selected
                                        clickCallback = props.clickCallback
                                    }
                                }
                            }
                        }
                }
            }
        } else {
            div("d-inline-block text-truncate") {
                span("tree-leaf") {}
                treeLabel(token)
            }
        }
    }
}

fun Element.renderObjectTree(
    visualObject: VisualObject,
    clickCallback: (Name) -> Unit = {}
) = render(this){
    card("Object tree") {
        child(ObjectTree::class) {
            attrs {
                this.name = Name.EMPTY
                this.obj = visualObject
                this.selected = null
                this.clickCallback = clickCallback
            }
        }
    }
}

fun RBuilder.objectTree(
    visualObject: VisualObject,
    selected: Name? = null,
    clickCallback: (Name) -> Unit = {}
){
    child(ObjectTree::class) {
        attrs {
            this.name = Name.EMPTY
            this.obj = visualObject
            this.selected = selected
            this.clickCallback = clickCallback
        }
    }
}

