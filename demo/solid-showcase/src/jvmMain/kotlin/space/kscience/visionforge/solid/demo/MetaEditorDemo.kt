package space.kscience.visionforge.demo

import javafx.geometry.Orientation
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.asConfig
import space.kscience.dataforge.meta.descriptors.NodeDescriptor
import space.kscience.dataforge.values.ValueType
import space.kscience.visionforge.editor.ConfigEditor
import space.kscience.visionforge.editor.FXMeta
import space.kscience.visionforge.editor.MetaViewer
import tornadofx.*


class MetaEditorDemoApp : App(MetaEditorDemo::class)

class MetaEditorDemo : View("Meta editor demo") {

    val meta = Meta {
        "aNode" put {
            "innerNode" put {
                "innerValue" put true
            }
            "b" put 223
            "c" put "StringValue"
        }
    }.asConfig()

    val descriptor = NodeDescriptor {
        node("aNode") {
            info = "A root demo node"
            value("b") {
                info = "b number value"
                type(ValueType.NUMBER)
            }
            node("otherNode") {
                value("otherValue") {
                    type(ValueType.BOOLEAN)
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

    private val rootNode = FXMeta.root(meta, descriptor)

    override val root =
        splitpane(Orientation.HORIZONTAL, MetaViewer(rootNode).root, ConfigEditor(
            rootNode
        ).root)
}

fun main() {
    launch<MetaEditorDemoApp>()
}