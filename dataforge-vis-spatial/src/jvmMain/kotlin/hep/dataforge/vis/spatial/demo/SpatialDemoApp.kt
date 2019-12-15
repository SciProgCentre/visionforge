package hep.dataforge.vis.spatial.demo

import hep.dataforge.context.Global
import hep.dataforge.meta.number
import hep.dataforge.vis.spatial.*
import hep.dataforge.vis.spatial.fx.Canvas3D
import hep.dataforge.vis.spatial.fx.FX3DPlugin
import javafx.scene.Parent
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import tornadofx.*
import kotlin.random.Random

class SpatialDemoApp: App(SpatialDemoView::class)

class SpatialDemoView: View(){
    private val plugin = Global.plugins.fetch(FX3DPlugin)
    private val canvas = Canvas3D(plugin)

    override val root: Parent = borderpane {
        center = canvas.root
    }

    lateinit var group: VisualGroup3D

    init {
        canvas.render {
            box(100,100,100)
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

        canvas.apply {
            angleY = -30.0
            angleX = -15.0
        }
    }
}


fun main() {
    launch<SpatialDemoApp>()
}