package space.kscience.visionforge.editor

import javafx.beans.binding.Binding
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.layout.VBox
import space.kscience.dataforge.meta.Config
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.MutableItemProvider
import space.kscience.dataforge.meta.descriptors.NodeDescriptor
import space.kscience.dataforge.meta.update
import space.kscience.visionforge.*
import tornadofx.*

class VisualObjectEditorFragment(val selector: (Vision) -> Meta) : Fragment() {

    val itemProperty = SimpleObjectProperty<Vision>()
    var item: Vision? by itemProperty
    val descriptorProperty = SimpleObjectProperty<NodeDescriptor>()

    constructor(
        item: Vision?,
        descriptor: NodeDescriptor?,
        selector: (Vision) -> MutableItemProvider = { it.allProperties() },
    ) : this({ it.describedProperties }) {
        this.item = item
        this.descriptorProperty.set(descriptor)
    }

    private var currentConfig: Config? = null

    private val configProperty: Binding<Config?> = itemProperty.objectBinding { visualObject ->
        if (visualObject == null) return@objectBinding null
        val meta = selector(visualObject)
        val config = Config().apply {
            update(meta)
            onChange(this@VisualObjectEditorFragment) { key, _, after ->
                visualObject.setProperty(key, after)
            }
        }
        //remember old config reference to cleanup listeners
        currentConfig?.removeListener(this)
        currentConfig = config
        config
    }

    private val configEditorProperty: Binding<Node?> = configProperty.objectBinding(descriptorProperty) {
        it?.let {
            ConfigEditor(it, descriptorProperty.get()).root
        }
    }

    private val styleBoxProperty: Binding<Node?> = configProperty.objectBinding() {
        VBox().apply {
            item?.styles?.forEach { styleName ->
                val styleMeta = item?.getStyle(styleName)
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