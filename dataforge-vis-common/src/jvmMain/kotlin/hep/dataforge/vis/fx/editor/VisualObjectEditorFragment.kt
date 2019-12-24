package hep.dataforge.vis.fx.editor

import hep.dataforge.descriptors.NodeDescriptor
import hep.dataforge.meta.Config
import hep.dataforge.vis.common.VisualObject
import hep.dataforge.vis.common.findStyle
import javafx.beans.binding.Binding
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.layout.VBox
import tornadofx.*

class VisualObjectEditorFragment(val selector: (VisualObject) -> Config) : Fragment() {

    val itemProperty = SimpleObjectProperty<VisualObject>()
    var item: VisualObject? by itemProperty
    val descriptorProperty = SimpleObjectProperty<NodeDescriptor>()

    constructor(
        item: VisualObject?,
        descriptor: NodeDescriptor?,
        selector: (VisualObject) -> Config = { it.config }
    ) : this(selector) {
        this.item = item
        this.descriptorProperty.set(descriptor)
    }

    private val configProperty: Binding<Config?> = itemProperty.objectBinding {
        it?.let { selector(it) }
    }

    private val configEditorProperty: Binding<Node?> = configProperty.objectBinding(descriptorProperty) {
        it?.let {
            ConfigEditor(it, descriptorProperty.get()).root
        }
    }

    private val styleBoxProperty: Binding<Node?> = configProperty.objectBinding() {
        VBox().apply {
            item?.styles?.forEach { styleName ->
                val styleMeta = item?.findStyle(styleName)
                if (styleMeta != null) {
                    titledpane(styleName, node = MetaViewer(styleMeta).root)
                }
            }
        }
    }

    override val root: Parent = vbox {
        titledpane("Properties", collapsible = false) {
            contentProperty().bind(configEditorProperty)
        }
        titledpane("Styles", collapsible = false) {
            visibleWhen(itemProperty.booleanBinding { it?.styles?.isNotEmpty() ?: false })
            contentProperty().bind(styleBoxProperty)
        }
    }
}