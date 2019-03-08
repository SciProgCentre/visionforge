package hep.dataforge.vis.spatial

import hep.dataforge.context.Global
import hep.dataforge.meta.number
import hep.dataforge.vis.DisplayGroup
import javafx.scene.Parent
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import tornadofx.*
import kotlin.random.Random


class RendererDemoApp : App(RendererDemoView::class)


class RendererDemoView : View() {
    val renderer = FX3DOutput(Global)
    override val root: Parent = borderpane {
        center = renderer.canvas.root
    }

    lateinit var group: DisplayGroup

    init {

        renderer.render {
            group = group {
                box {
                    xSize = 100.0
                    ySize = 100.0
                    zSize = 100.0
                }
                box {
                    x = 110.0
                    xSize = 100.0
                    ySize = 100.0
                    zSize = 100.0
                }
            }
        }

        var color by group.properties.number(1530).int

        GlobalScope.launch {
            val random = Random(111)
            while (isActive) {
                delay(1000)
                color = random.nextInt(0, Int.MAX_VALUE)
            }
        }

        renderer.canvas.apply {
            angleY = -30.0
            angleX = -15.0
        }
    }
}


fun main() {
    launch<RendererDemoApp>()
}