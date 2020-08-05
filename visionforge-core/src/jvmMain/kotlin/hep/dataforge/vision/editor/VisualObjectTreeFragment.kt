package hep.dataforge.vision.editor

import hep.dataforge.vision.VisualGroup
import hep.dataforge.vision.VisualObject
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.SelectionMode
import javafx.scene.control.TreeItem
import tornadofx.*

private fun toTreeItem(visualObject: VisualObject, title: String): TreeItem<Pair<String, VisualObject>> {
    return object : TreeItem<Pair<String, VisualObject>>(title to visualObject) {
        init {
            if (visualObject is VisualGroup) {
                //lazy populate the tree
                expandedProperty().onChange { expanded ->
                    if (expanded && children.isEmpty()) {
                        children.setAll(visualObject.children.map {
                            toTreeItem(it.value, it.key.toString())
                        })
                    }
                }
            }
        }

        override fun isLeaf(): Boolean {
            return !(visualObject is VisualGroup && visualObject.children.isNotEmpty())
        }
    }
}


class VisualObjectTreeFragment : Fragment() {
    val itemProperty = SimpleObjectProperty<VisualObject>()
    var item: VisualObject? by itemProperty

    val selectedProperty = SimpleObjectProperty<VisualObject>()

    override val root = vbox {
        titledpane("Object tree", collapsible = false) {
            treeview<Pair<String, VisualObject>> {
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