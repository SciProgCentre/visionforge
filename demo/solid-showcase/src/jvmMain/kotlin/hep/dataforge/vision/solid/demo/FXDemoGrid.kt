package hep.dataforge.vision.solid.demo

import hep.dataforge.context.Global
import hep.dataforge.meta.Meta
import hep.dataforge.names.Name
import hep.dataforge.vision.VisionLayout
import hep.dataforge.vision.solid.FX3DPlugin
import hep.dataforge.vision.solid.FXCanvas3D
import hep.dataforge.vision.solid.Solid
import javafx.collections.FXCollections
import javafx.scene.Parent
import javafx.scene.control.Tab
import tornadofx.*

class FXDemoGrid : View(title = "DataForge-vis FX demo"), VisionLayout<Solid> {
    private val outputs = FXCollections.observableHashMap<Name, FXCanvas3D>()

    override val root: Parent = borderpane {
        center = tabpane {
            tabs.bind(outputs) { key: Name, value: FXCanvas3D ->
                Tab(key.toString(), value.root)
            }
        }
    }

    private val fx3d = Global.plugins.fetch(FX3DPlugin)

    override fun render(name: Name, vision: Solid, meta: Meta) {
        outputs.getOrPut(name) { FXCanvas3D(fx3d, canvasOptions) }.render(vision)
    }

}