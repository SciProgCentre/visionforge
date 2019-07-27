package hep.dataforge.vis.spatial

import hep.dataforge.context.Global
import hep.dataforge.meta.number
import hep.dataforge.vis.common.VisualGroup
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

    lateinit var group: VisualGroup

    init {

        renderer.render {
            group = group {
                box(100,100,100)
                box(100,100,100) {
                    x = 110.0
                }
            }
        }

        var color by group.config.number(1530).int

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