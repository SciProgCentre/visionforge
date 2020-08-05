package hep.dataforge.vision.spatial.demo

import hep.dataforge.context.Global
import hep.dataforge.meta.Meta
import hep.dataforge.names.Name
import hep.dataforge.output.OutputManager
import hep.dataforge.output.Renderer
import hep.dataforge.vision.Vision
import hep.dataforge.vision.spatial.fx.FX3DPlugin
import hep.dataforge.vision.spatial.fx.FXCanvas3D
import javafx.collections.FXCollections
import javafx.scene.Parent
import javafx.scene.control.Tab
import tornadofx.*
import kotlin.reflect.KClass

class FXDemoGrid : View(title = "DataForge-vis FX demo"), OutputManager {
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
            if (type != Vision::class) kotlin.error("Supports only DisplayObject")
            val output = FXCanvas3D(fx3d, canvasOptions)

            output
        } as Renderer<T>
    }

}