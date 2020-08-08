package hep.dataforge.vision.bootstrap

import hep.dataforge.names.Name
import hep.dataforge.names.plus
import hep.dataforge.names.startsWith
import hep.dataforge.vision.Vision
import hep.dataforge.vision.VisionGroup
import hep.dataforge.vision.isEmpty
import hep.dataforge.vision.react.RFBuilder
import hep.dataforge.vision.react.component
import kotlinx.html.classes
import kotlinx.html.js.onClickFunction
import org.w3c.dom.Element
import org.w3c.dom.events.Event
import react.*
import react.dom.*

interface ObjectTreeProps : RProps {
    var name: Name
    var selected: Name?
    var obj: Vision
    var clickCallback: (Name) -> Unit
}

interface TreeState : RState {
    var expanded: Boolean
}

private fun RFBuilder.objectTree(props: ObjectTreeProps): Unit {
    var expanded: Boolean by useState{ props.selected?.startsWith(props.name) ?: false }

    val onClick: (Event) -> Unit = {
        expanded = !expanded
    }

    fun RBuilder.treeLabel(text: String) {
        button(classes = "btn btn-link align-middle tree-label p-0") {
            +text
            attrs {
                if (props.name == props.selected) {
                    classes += "tree-label-selected"
                }
                onClickFunction = { props.clickCallback(props.name) }
            }
        }
    }

    val token = props.name.last()?.toString() ?: "World"
    val obj = props.obj

    //display as node if any child is visible
    if (obj is VisionGroup) {
        div("d-block text-truncate") {
            if (obj.children.any { !it.key.body.startsWith("@") }) {
                span("tree-caret") {
                    attrs {
                        if (expanded) {
                            classes += "tree-caret-down"
                        }
                        onClickFunction = onClick
                    }
                }
            }
            treeLabel(token)
        }
        if (expanded) {
            ul("tree") {
                obj.children.entries
                    .filter { !it.key.toString().startsWith("@") } // ignore statics and other hidden children
                    .sortedBy { (it.value as? VisionGroup)?.isEmpty ?: true }
                    .forEach { (childToken, child) ->
                        li("tree-item") {
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
        div("d-block text-truncate") {
            span("tree-leaf") {}
            treeLabel(token)
        }
    }
}

val ObjectTree: FunctionalComponent<ObjectTreeProps> = component { props ->
    objectTree(props)
}

fun Element.renderObjectTree(
    vision: Vision,
    clickCallback: (Name) -> Unit = {}
) = render(this) {
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

