package hep.dataforge.vis.demo

import hep.dataforge.meta.Meta
import hep.dataforge.meta.asConfig
import hep.dataforge.meta.descriptors.NodeDescriptor
import hep.dataforge.values.ValueType
import hep.dataforge.vis.editor.ConfigEditor
import hep.dataforge.vis.editor.FXMeta
import hep.dataforge.vis.editor.MetaViewer
import javafx.geometry.Orientation
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