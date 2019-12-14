package hep.dataforge.vis.spatial.fx

import hep.dataforge.context.Global
import hep.dataforge.meta.number
import hep.dataforge.vis.spatial.*
import javafx.scene.Parent
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import tornadofx.*
import kotlin.random.Random


class RendererDemoApp : App(RendererDemoView::class)


class RendererDemoView : View() {
    val plugin = Global.plugins.fetch(FX3DPlugin)
    val renderer = Canvas3D(plugin)
    override val root: Parent = borderpane {
        center = renderer.root
    }

    lateinit var group: VisualGroup3D

    init {

        renderer.render {
            group = group {
                box(100,100,100)
                box(100,100,100) {
                    x = 110.0
                }
            }
        }

        var color by group.config.number(1530)

        GlobalScope.launch {
            val random = Random(111)
            while (isActive) {
                delay(1000)
                color = random.nextInt(0, Int.MAX_VALUE)
            }
        }

        renderer.apply {
            angleY = -30.0
            angleX = -15.0
        }
    }
}


fun main() {
    launch<RendererDemoApp>()
}