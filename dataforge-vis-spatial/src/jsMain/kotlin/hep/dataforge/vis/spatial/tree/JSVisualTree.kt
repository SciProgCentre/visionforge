package hep.dataforge.vis.spatial.tree

import hep.dataforge.vis.common.VisualGroup
import hep.dataforge.vis.common.VisualObject
import hep.dataforge.vis.spatial.visible
import kotlinx.html.InputType
import kotlinx.html.dom.append
import kotlinx.html.js.*
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.Node
import kotlin.browser.document

class JSVisualTree(
    val title: String,
    override val item: VisualObject,
    val callback: VisualObject.() -> Unit
) : VisualTree<VisualObject> {

    override val children: Collection<JSVisualTree> by lazy {
        if (item is VisualGroup) {
            item.children.mapNotNull {(key,value)->
                val str = key.toString()
                if(!str.startsWith("@")) {
                    JSVisualTree(str, value, callback)
                } else {
                     null
                }
            }
        } else {
            emptyList()
        }
    }

    var visible: Boolean
        get() = item.visible ?: true
        set(value) {
            item.visible = value
        }

    val root: Node by lazy {
        (document.createElement("div") as HTMLDivElement).apply {
            append {
                div(TREE_ITEM_HEADER_CLASS) {
                    input(type = InputType.checkBox).apply {
                        checked = visible
                        onChangeFunction = {
                            visible = checked
                        }
                    }
                    a {
                        +this@JSVisualTree.title
                        +"[${item::class}]"
                    }
                }
                if (item is VisualGroup) {
                    ul {
                        this@JSVisualTree.children.forEach { value ->
                            li(TREE_NODE_CLASS).apply {
                                appendChild(value.root)
                            }
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val TREE_NODE_CLASS = "tree-node"
        const val TREE_LEAF_CLASS = "tree-leaf"
        const val TREE_ITEM_HEADER_CLASS = "tree-header"
    }

}