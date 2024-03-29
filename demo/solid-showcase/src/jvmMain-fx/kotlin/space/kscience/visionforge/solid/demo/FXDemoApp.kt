package space.kscience.visionforge.solid.demo

import javafx.stage.Stage
import tornadofx.*

class FXDemoApp : App(FXDemoGrid::class) {

    val view: FXDemoGrid by inject()

    override fun start(stage: Stage) {
        super.start(stage)

        stage.width = 600.0
        stage.height = 600.0

        view.showcase()
        //view.showcaseCSG()
    }
}

fun main() {
    launch<FXDemoApp>()
}