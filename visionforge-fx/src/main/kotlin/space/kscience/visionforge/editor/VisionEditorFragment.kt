package space.kscience.visionforge.editor

import javafx.beans.binding.Binding
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.layout.VBox
import space.kscience.dataforge.meta.MutableMeta
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.dataforge.names.Name
import space.kscience.visionforge.Vision
import space.kscience.visionforge.getStyle
import space.kscience.visionforge.root
import space.kscience.visionforge.styles
import tornadofx.*

public class VisionEditorFragment : Fragment() {

    public val visionProperty: SimpleObjectProperty<Vision> = SimpleObjectProperty<Vision>()
    public var vision: Vision? by visionProperty
    public val descriptorProperty: SimpleObjectProperty<MetaDescriptor> = SimpleObjectProperty<MetaDescriptor>()

    private val configProperty: Binding<MutableMeta?> = visionProperty.objectBinding { vision ->
        vision?.properties?.root()
    }

    private val configEditorProperty: Binding<Node?> = configProperty.objectBinding(descriptorProperty) {
        it?.let { meta ->
            val node:FXMetaModel<MutableMeta> = FXMetaModel(
                meta,
                vision?.descriptor,
                vision?.properties?.root(),
                Name.EMPTY,
                "Vision properties"
            )
            MutableMetaEditor(node).root
        }
    }

    private val styleBoxProperty: Binding<Node?> = configProperty.objectBinding {
        VBox().apply {
            vision?.styles?.forEach { styleName ->
                val styleMeta = vision?.getStyle(styleName)
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
            visibleWhen(visionProperty.booleanBinding { it?.styles?.isNotEmpty() ?: false })
            contentProperty().bind(styleBoxProperty)
        }
    }
}