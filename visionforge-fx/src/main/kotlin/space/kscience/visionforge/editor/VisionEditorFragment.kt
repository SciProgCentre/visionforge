package space.kscience.visionforge.editor

import javafx.beans.binding.Binding
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.layout.VBox
import space.kscience.dataforge.meta.*
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.visionforge.*
import tornadofx.*

public class VisionEditorFragment(public val selector: (Vision) -> Meta) : Fragment() {

    public val itemProperty: SimpleObjectProperty<Vision> = SimpleObjectProperty<Vision>()
    public var item: Vision? by itemProperty
    public val descriptorProperty: SimpleObjectProperty<MetaDescriptor> = SimpleObjectProperty<MetaDescriptor>()

    public constructor(
        item: Vision?,
        descriptor: MetaDescriptor?,
        selector: (Vision) -> MutableMetaProvider = { it.meta() },
    ) : this({ it.describedProperties }) {
        this.item = item
        this.descriptorProperty.set(descriptor)
    }

    private var currentConfig: ObservableMutableMeta? = null

    private val configProperty: Binding<ObservableMutableMeta?> = itemProperty.objectBinding { vision ->
        if (vision == null) return@objectBinding null
        val meta = selector(vision)
        val config = MutableMeta {
            update(meta)
        }
        config.onChange(this@VisionEditorFragment) { key ->
            vision.setPropertyNode(key, config[key])
        }
        //remember old config reference to cleanup listeners
        currentConfig?.removeListener(this)
        currentConfig = config
        config
    }

    private val configEditorProperty: Binding<Node?> = configProperty.objectBinding(descriptorProperty) {
        it?.let {
            MutableMetaEditor(it, descriptorProperty.get()).root
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