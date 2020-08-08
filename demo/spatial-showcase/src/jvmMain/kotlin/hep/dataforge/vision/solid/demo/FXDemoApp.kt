package hep.dataforge.vision.solid.demo

import hep.dataforge.vision.solid.gdml.gdml
import javafx.stage.Stage
import tornadofx.*
import java.nio.file.NoSuchFileException
import java.nio.file.Paths

class FXDemoApp : App(FXDemoGrid::class) {

    val view: FXDemoGrid by inject()

    override fun start(stage: Stage) {
        super.start(stage)

        stage.width = 600.0
        stage.height = 600.0

        view.showcase()
        try {
            view.demo("gdml", "gdml-cubes") {
                gdml(Paths.get("D:\\Work\\Projects\\gdml.kt\\gdml-source\\cubes.gdml"))
                //setProperty(Material3D.MATERIAL_WIREFRAME_KEY, true)
            }
        }
        catch (e: NoSuchFileException) {
            println("GDML demo: Please specify the correct file path e.g. " +
                    "visionforge\\demo\\gdml\\src\\commonMain\\resources\\cubes.gdml")
        }
    }
}

fun main() {
    launch<FXDemoApp>()
}