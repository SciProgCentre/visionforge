package hep.dataforge.vis.fx.demo

import hep.dataforge.descriptors.NodeDescriptor
import hep.dataforge.meta.buildMeta
import hep.dataforge.meta.toConfig
import hep.dataforge.values.ValueType
import hep.dataforge.vis.fx.meta.ConfigEditor
import hep.dataforge.vis.fx.meta.FXMeta
import hep.dataforge.vis.fx.meta.MetaViewer
import javafx.geometry.Orientation
import tornadofx.*


class MetaEditorDemoApp : App(MetaEditorDemo::class)

class MetaEditorDemo : View("Meta editor demo") {

    val meta = buildMeta {
        "aNode" to {
            "innerNode" to {
                "innerValue" to true
            }
            "b" to 223
            "c" to "StringValue"
        }
    }.toConfig()

    val descriptor = NodeDescriptor.build {
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
        value("multiple"){
            info = "A sns value"
            multiple = true
        }
    }

    private val rootNode = FXMeta.root(meta,descriptor)

    override val root =
        splitpane(Orientation.HORIZONTAL, MetaViewer(rootNode).root, ConfigEditor(rootNode).root)
}

fun main() {
    launch<MetaEditorDemoApp>()
}
