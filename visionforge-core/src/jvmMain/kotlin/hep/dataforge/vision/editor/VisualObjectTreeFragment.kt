package hep.dataforge.vision.editor

import hep.dataforge.vision.Vision
import hep.dataforge.vision.VisionGroup
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.SelectionMode
import javafx.scene.control.TreeItem
import tornadofx.*

private fun toTreeItem(vision: Vision, title: String): TreeItem<Pair<String, Vision>> {
    return object : TreeItem<Pair<String, Vision>>(title to vision) {
        init {
            if (vision is VisionGroup) {
                //lazy populate the tree
                expandedProperty().onChange { expanded ->
                    if (expanded && children.isEmpty()) {
                        children.setAll(vision.children.map {
                            toTreeItem(it.value, it.key.toString())
                        })
                    }
                }
            }
        }

        override fun isLeaf(): Boolean {
            return !(vision is VisionGroup && vision.children.isNotEmpty())
        }
    }
}


class VisualObjectTreeFragment : Fragment() {
    val itemProperty = SimpleObjectProperty<Vision>()
    var item: Vision? by itemProperty

    val selectedProperty = SimpleObjectProperty<Vision>()

    override val root = vbox {
        titledpane("Object tree", collapsible = false) {
            treeview<Pair<String, Vision>> {
                cellFormat {
                    text = item.first
                }
                itemProperty.onChange { rootObject ->
                    if (rootObject != null) {
                        root = toTreeItem(rootObject, "world")
                    }
                }
                selectionModel.selectionMode = SelectionMode.SINGLE
                val selectedValue = selectionModel.selectedItemProperty().objectBinding { it?.value?.second }
                selectedProperty.bind(selectedValue)
            }
        }
    }
}