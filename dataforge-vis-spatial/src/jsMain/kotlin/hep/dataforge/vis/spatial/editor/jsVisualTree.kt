@file:Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")

package hep.dataforge.vis.spatial.editor

import hep.dataforge.meta.string
import hep.dataforge.names.EmptyName
import hep.dataforge.names.Name
import hep.dataforge.names.NameToken
import hep.dataforge.vis.common.VisualGroup
import hep.dataforge.vis.common.VisualObject
import hep.dataforge.vis.common.getProperty
import hep.dataforge.vis.jsObject
import hep.dataforge.vis.spatial.Proxy
import hep.dataforge.vis.spatial.visible
import info.laht.threekt.loaders.Cache.clear
import kotlinx.html.div
import kotlinx.html.dom.append
import org.w3c.dom.Element
import kotlin.dom.clear
import kotlin.js.json

operator fun Name.plus(other: NameToken): Name = Name(tokens + other)


private fun createInspireTree(block: Config.() -> Unit = {}): InspireTree {
    val config = (json(
        "checkbox" to json(
            "autoCheckChildren" to false
        )
    ) as Config).apply(block)
    return InspireTree(config)
}

private fun VisualObject.toTree(onFocus: (VisualObject?, String?) -> Unit = { _, _ -> }): InspireTree {

    val map = HashMap<String, VisualObject>()

    fun generateNodeConfig(item: VisualObject, fullName: Name): NodeConfig {
        val title = item.getProperty("title").string ?: fullName.last()?.toString() ?: "root"
        val className = if (item is Proxy) {
            item.prototype::class.toString()
        } else {
            item::class.toString()
        }.replace("class ", "")

        val text: String = if (title.startsWith("@")) {
            "[$className}]"
        } else {
            "$title[$className}]"
        }

        return json(
            "children" to if ((item as? VisualGroup)?.children?.isEmpty() != false) {
                emptyArray<NodeConfig>()
            } else {
                true
            },
            "text" to text,
            "id" to fullName.toString(),
            "itree" to json(
                "state" to json(
                    "checked" to (item.visible ?: true)
                )
            )
        ) as NodeConfig

    }

    fun TreeNode.fillChildren(group: VisualObject, groupName: Name) {
        if(group is VisualGroup) {
            group.children.forEach { (token, obj) ->
                if (!token.body.startsWith("@")) {
                    val name = groupName + token
                    val nodeConfig = generateNodeConfig(obj, name)
                    val childNode = addChild(nodeConfig)
                    map[childNode.id] = obj
                    if (obj is VisualGroup) {
                        childNode.fillChildren(obj, name)
                    }
                }
            }
        }
    }

    val inspireTree = createInspireTree {

    }
    val nodeConfig = generateNodeConfig(this, EmptyName)
    val rootNode = inspireTree.addNode(nodeConfig)
    map[rootNode.id] = this
    rootNode.fillChildren(this, EmptyName)

//    inspireTree.on("node.selected") { node: TreeNode, isLoadEvent: Boolean ->
//        if (!isLoadEvent) {
//            map[node.id]?.selected = node.selected()
//        }
//    }
//
//    inspireTree.on("node.deselect") { node: TreeNode ->
//        map[node.id]?.selected = node.selected()
//    }

    inspireTree.on("node.checked") { node: TreeNode, isLoadEvent: Boolean ->
        if (!isLoadEvent) {
            map[node.id]?.visible = node.checked()
        }
    }

    inspireTree.on("node.unchecked") { node: TreeNode ->
        if (!node.indeterminate()) {
            map[node.id]?.visible = node.checked()
        }
    }

    inspireTree.on("node.focused") { node: TreeNode, isLoadEvent: Boolean ->
        if (!isLoadEvent) {
            onFocus(map[node.id], node.id)
        }
    }

    inspireTree.collapseDeep()

    return inspireTree
}

fun Element.visualObjectTree(group: VisualObject, onFocus: (VisualObject?, String?) -> Unit) {
    this.clear()
    append {
        card("Visual object tree") {
            val domConfig = jsObject<DomConfig> {
                target = div()
                showCheckboxes = false
            }
            InspireTreeDOM(group.toTree(onFocus), domConfig)
        }
    }
}