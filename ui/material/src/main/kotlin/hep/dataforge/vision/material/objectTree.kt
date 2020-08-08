package hep.dataforge.vision.material

import hep.dataforge.names.Name
import hep.dataforge.names.plus
import hep.dataforge.names.toName
import hep.dataforge.vision.Vision
import hep.dataforge.vision.VisionGroup
import hep.dataforge.vision.isEmpty
import hep.dataforge.vision.react.component
import hep.dataforge.vision.react.state
import kotlinx.html.UL
import materialui.lab.components.treeItem.treeItem
import materialui.lab.components.treeView.SingleSelectTreeViewElementBuilder
import materialui.lab.components.treeView.treeView
import react.FunctionalComponent
import react.RBuilder
import react.RProps
import react.child
import react.dom.span

interface ObjectTreeProps : RProps {
    var name: Name
    var selected: Name?
    var obj: Vision
    var clickCallback: (Name?) -> Unit
}

private fun RBuilder.treeBranch(name: Name, obj: Vision): Unit {
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

        if (obj is VisionGroup) {
            obj.children.entries
                .filter { !it.key.toString().startsWith("@") } // ignore statics and other hidden children
                .sortedBy { (it.value as? VisionGroup)?.isEmpty ?: true }
                .forEach { (childToken, child) ->
                    treeBranch(name + childToken, child)
                }
        }
    }
}

val ObjectTree: FunctionalComponent<ObjectTreeProps> = component { props ->
    var selected: String? by state { props.selected.toString() }
    treeView {
        this as SingleSelectTreeViewElementBuilder<UL>
        attrs {
            this.selected = selected
            this.onNodeSelect{ _, selectedItem ->
                selected = selectedItem
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
    vision: Vision,
    selected: Name? = null,
    clickCallback: (Name?) -> Unit = {}
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

