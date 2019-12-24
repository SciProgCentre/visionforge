package hep.dataforge.vis.fx.editor

import hep.dataforge.vis.common.VisualGroup
import hep.dataforge.vis.common.VisualObject
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.SelectionMode
import javafx.scene.control.TreeItem
import tornadofx.*

class VisualObjectTreeFragment : Fragment() {
    val itemProperty = SimpleObjectProperty<VisualObject>()
    var item: VisualObject? by itemProperty

    val selectedProperty = SimpleObjectProperty<VisualObject>()

    override val root = borderpane{
        center = titledpane("Object tree") {
            treeview<VisualObject> {
                itemProperty.onChange { rootObject ->
                    if (rootObject != null) {
                        root = TreeItem(rootObject)
                        populate { item ->
                            (item.value as? VisualGroup)?.children?.values?.toList()
                        }
                    }
                }
                selectionModel.selectionMode = SelectionMode.SINGLE
                val selectedValue = selectionModel.selectedItemProperty().objectBinding{it?.value}
                selectedProperty.bind(selectedValue)
            }
        }
    }
}