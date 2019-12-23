package hep.dataforge.vis.spatial.demo

import hep.dataforge.vis.spatial.Material3D
import hep.dataforge.vis.spatial.gdml.LUnit
import hep.dataforge.vis.spatial.gdml.gdml
import javafx.stage.Stage
import tornadofx.*
import java.nio.file.Paths

class FXDemoApp : App(FXDemoGrid::class) {

    val view: FXDemoGrid by inject()

    override fun start(stage: Stage) {
        super.start(stage)

        stage.width = 400.0
        stage.height = 400.0

        view.showcase()
        view.demo("gdml", "gdml") {
            gdml(Paths.get("D:\\Work\\Projects\\gdml.kt\\gdml-source\\cubes.gdml")) {
                lUnit = LUnit.CM

                solidConfiguration = { parent, solid ->
                    if (parent.physVolumes.isNotEmpty()) {
                        useStyle("opaque") {
                            Material3D.MATERIAL_OPACITY_KEY put 0.3
                        }
                    }
                }
            }
            //setProperty(Material3D.MATERIAL_WIREFRAME_KEY, true)
        }
    }
}

fun main() {
    launch<FXDemoApp>()
}