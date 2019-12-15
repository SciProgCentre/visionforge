package hep.dataforge.vis.spatial.demo

import hep.dataforge.context.Global
import hep.dataforge.meta.Meta
import hep.dataforge.meta.buildMeta
import hep.dataforge.names.Name
import hep.dataforge.names.toName
import hep.dataforge.output.OutputManager
import hep.dataforge.output.Renderer
import hep.dataforge.vis.common.VisualObject
import hep.dataforge.vis.spatial.VisualGroup3D
import hep.dataforge.vis.spatial.fx.FX3DPlugin
import hep.dataforge.vis.spatial.fx.FXCanvas3D
import hep.dataforge.vis.spatial.render
import javafx.collections.FXCollections
import javafx.scene.Parent
import javafx.scene.control.Tab
import tornadofx.*
import kotlin.reflect.KClass

class FXDemoGrid : View(), OutputManager {
    private val outputs = FXCollections.observableHashMap<Name, FXCanvas3D>()

    override val root: Parent = borderpane {
        center = tabpane {
            tabs.bind(outputs) { key: Name, value: FXCanvas3D ->
                Tab(key.toString(), value.root)
            }
        }
    }

    private val fx3d = Global.plugins.fetch(FX3DPlugin)

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> get(type: KClass<out T>, name: Name, stage: Name, meta: Meta): Renderer<T> {
        return outputs.getOrPut(name) {
            if (type != VisualObject::class) kotlin.error("Supports only DisplayObject")
            val customMeta = buildMeta(meta) {
                "minSize" put 500
                "axis" put {
                    "size" put 500
                }
            }
            val output = FXCanvas3D(fx3d, customMeta)

            output
        } as Renderer<T>
    }

}

fun FXDemoGrid.demo(name: String, title: String = name, block: VisualGroup3D.() -> Unit) {
    val meta = buildMeta {
        "title" put title
    }
    val output = get(VisualObject::class, name.toName(), meta = meta)
    output.render(action = block)
}