package space.kscience.visionforge.solid.demo

import javafx.collections.FXCollections
import javafx.scene.Parent
import javafx.scene.control.Tab
import space.kscience.dataforge.context.Global
import space.kscience.dataforge.context.fetch
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.names.Name
import space.kscience.visionforge.VisionLayout
import space.kscience.visionforge.solid.FX3DPlugin
import space.kscience.visionforge.solid.FXCanvas3D
import space.kscience.visionforge.solid.Solid
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

    private val fx3d = Global.fetch(FX3DPlugin)

    override fun render(name: Name, vision: Solid, meta: Meta) {
        outputs.getOrPut(name) { FXCanvas3D(fx3d, canvasOptions) }.render(vision)
    }

}