package space.kscience.visionforge.demo

import javafx.geometry.Orientation
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.MutableMeta
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.dataforge.meta.descriptors.node
import space.kscience.dataforge.meta.descriptors.value
import space.kscience.dataforge.values.ValueType
import space.kscience.visionforge.editor.FXMetaModel
import space.kscience.visionforge.editor.MetaViewer
import space.kscience.visionforge.editor.MutableMetaEditor
import tornadofx.*


class MetaEditorDemoApp : App(MetaEditorDemo::class)

class MetaEditorDemo : View("Meta editor demo") {

    val meta = MutableMeta {
        "aNode" put {
            "innerNode" put {
                "innerValue" put true
            }
            "b" put 223
            "c" put "StringValue"
        }
    }

    val descriptor = MetaDescriptor {
        node("aNode") {
            info = "A root demo node"
            value("b", ValueType.NUMBER) {
                info = "b number value"
            }
            node("otherNode") {
                value("otherValue", ValueType.BOOLEAN) {
                    default(false)
                    info = "default value"
                }
            }
        }
        node("multiple") {
            info = "A sns value"
            multiple = true
        }
    }

    private val rootNode = FXMetaModel.root(meta, descriptor)

    override val root = splitpane(
        Orientation.HORIZONTAL,
        MetaViewer(rootNode as Meta).root,
        MutableMetaEditor(rootNode as FXMetaModel<MutableMeta>).root
    )
}

fun main() {
    launch<MetaEditorDemoApp>()
}