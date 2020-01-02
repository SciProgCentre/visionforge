package hep.dataforge.vis.spatial.demo

import hep.dataforge.vis.spatial.gdml.gdml
import javafx.stage.Stage
import tornadofx.*
import java.nio.file.Paths

class FXDemoApp : App(FXDemoGrid::class) {

    val view: FXDemoGrid by inject()

    override fun start(stage: Stage) {
        super.start(stage)

        stage.width = 600.0
        stage.height = 600.0

        view.showcase()
        view.demo("gdml", "gdml-cubes") {
            gdml(Paths.get("D:\\Work\\Projects\\gdml.kt\\gdml-source\\cubes.gdml"))
            //setProperty(Material3D.MATERIAL_WIREFRAME_KEY, true)
        }
    }
}

fun main() {
    launch<FXDemoApp>()
}