package hep.dataforge.vis.material

import hep.dataforge.names.Name
import hep.dataforge.names.plus
import hep.dataforge.names.toName
import hep.dataforge.vis.VisualGroup
import hep.dataforge.vis.VisualObject
import hep.dataforge.vis.isEmpty
import hep.dataforge.vis.react.component
import hep.dataforge.vis.react.state
import materialui.lab.components.treeItem.treeItem
import materialui.lab.components.treeView.treeView
import react.FunctionalComponent
import react.RBuilder
import react.RProps
import react.child
import react.dom.span

interface ObjectTreeProps : RProps {
    var name: Name
    var selected: Name?
    var obj: VisualObject
    var clickCallback: (Name?) -> Unit
}

private fun RBuilder.treeBranch(name: Name, obj: VisualObject): Unit {
    treeItem {
        val token = name.last()?.toString() ?: "World"
        attrs {
            nodeId = name.toString()
            label {
                span {
                    +token
                }
            }
        }

        if (obj is VisualGroup) {
            obj.children.entries
                .filter { !it.key.toString().startsWith("@") } // ignore statics and other hidden children
                .sortedBy { (it.value as? VisualGroup)?.isEmpty ?: true }
                .forEach { (childToken, child) ->
                    treeBranch(name + childToken, child)
                }
        }
    }
}

val ObjectTree: FunctionalComponent<ObjectTreeProps> = component { props ->
    var selected: String? by state { props.selected.toString() }
    treeView {
        attrs {
            this.selected = selected
            this.onNodeSelect = { _, selectedItem ->
                @Suppress("CAST_NEVER_SUCCEEDS")
                selected = (selectedItem as? String)
                val itemName = selected?.toName()
                props.clickCallback(itemName)
                Unit
            }
            defaultCollapseIcon {
                span{
                    +"-"
                }
                //child(ExpandMoreIcon::class) {}
            }//{<ExpandMoreIcon />}
            defaultExpandIcon {
                span{
                    +"+"
                }
                //child(ChevronRightIcon::class) {}
            }//{<ChevronRightIcon />}
        }
        treeBranch(props.name, props.obj)
    }
}

fun RBuilder.objectTree(
    visualObject: VisualObject,
    selected: Name? = null,
    clickCallback: (Name?) -> Unit = {}
) {
    child(ObjectTree) {
        attrs {
            this.name = Name.EMPTY
            this.obj = visualObject
            this.selected = selected
            this.clickCallback = clickCallback
        }
    }
}
