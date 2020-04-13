package hep.dataforge.vis.editor

import hep.dataforge.names.Name
import hep.dataforge.names.plus
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
    var obj: VisualObject
    var clickCallback: (Name) -> Unit
}

interface TreeState : RState {
    var expanded: Boolean
}

class ObjectTreeComponent : RComponent<ObjectTreeProps, TreeState>() {

    override fun TreeState.init() {
        expanded = false
    }

    private val onClick: (Event) -> Unit = {
        setState {
            expanded = !expanded
        }
    }

    override fun RBuilder.render() {
        val token = props.name.last()?.toString() ?: "World"
        val obj = props.obj

        //display as node if any child is visible
        if (obj is VisualGroup && obj.children.keys.any { !it.body.startsWith("@") }) {
            div("d-inline-block text-truncate") {
                span("tree-caret") {
                    attrs {
                        if (state.expanded) {
                            classes += "tree-caret-down"
                        }
                        onClickFunction = onClick
                    }
                }
                a("#",classes = "tree-label") {
                    +token
                    attrs {
                        onClickFunction = { props.clickCallback(props.name) }
                    }
                }
            }
            if (state.expanded) {
                ul("tree") {
                    obj.children.entries
                        .filter { !it.key.toString().startsWith("@") } // ignore statics and other hidden children
                        .sortedBy { (it.value as? VisualGroup)?.isEmpty ?: true }
                        .forEach { (childToken, child) ->
                            li("tree-item") {
                                child(ObjectTreeComponent::class) {
                                    attrs {
                                        name = props.name + childToken
                                        this.obj = child
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
                a("#",classes = "tree-label") {
                    +token
                    attrs {
                        onClickFunction = { props.clickCallback(props.name) }
                    }
                }
            }
        }
    }
}

fun RBuilder.objectTree(
    obj: VisualObject,
    clickCallback: (Name) -> Unit = {}
) = card("Object tree") {
    child(ObjectTreeComponent::class) {
        attrs {
            name = Name.EMPTY
            this.obj = obj
            this.clickCallback = clickCallback
        }
    }
}

fun Element.objectTree(
    obj: VisualObject,
    clickCallback: (Name) -> Unit = {}
) {
    render(this) {
        objectTree(obj, clickCallback)
    }
}