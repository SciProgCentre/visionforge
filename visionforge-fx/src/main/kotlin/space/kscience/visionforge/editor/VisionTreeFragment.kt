package space.kscience.visionforge.editor

import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.SelectionMode
import javafx.scene.control.TreeItem
import javafx.scene.layout.VBox
import space.kscience.visionforge.Vision
import space.kscience.visionforge.VisionGroup
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


public class VisionTreeFragment : Fragment() {
    public val itemProperty: SimpleObjectProperty<Vision> = SimpleObjectProperty<Vision>()
    public var item: Vision? by itemProperty

    public val selectedProperty: SimpleObjectProperty<Vision> = SimpleObjectProperty<Vision>()

    override val root: VBox = vbox {
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
                val selectedValue = selectionModel.selectedItemProperty().objectBinding {
                    it?.value?.second
                }
                selectedProperty.bind(selectedValue)
            }
        }
    }
}