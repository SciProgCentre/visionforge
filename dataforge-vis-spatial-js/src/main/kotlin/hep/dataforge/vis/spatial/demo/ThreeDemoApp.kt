package hep.dataforge.vis.spatial.demo

import hep.dataforge.context.ContextBuilder
import hep.dataforge.meta.number
import hep.dataforge.vis.spatial.*
import hep.dataforge.vis.spatial.jsroot.JSRootPlugin
import hep.dataforge.vis.spatial.jsroot.jsRootGeometry
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random


class ThreeDemoApp : ApplicationBase() {

    override val stateKeys: List<String> = emptyList()

    override fun start(state: Map<String, Any>) {
        require("JSRootGeoBase.js")

        //TODO replace by optimized builder after dataforge 0.1.3-dev-8
        val context = ContextBuilder("three-demo").apply {
            plugin(JSRootPlugin())
        }.build()

        val grid = context.plugins.load(ThreeDemoGrid()).apply {
            demo("group", "Group demo") {
                val group = group {
                    box {
                        z = 110.0
                        xSize = 100.0
                        ySize = 100.0
                        zSize = 100.0
                    }
                    box {
                        visible = false
                        x = 110.0
                        xSize = 100.0
                        ySize = 100.0
                        zSize = 100.0
                        color(1530)

                        GlobalScope.launch {
                            while (isActive) {
                                delay(500)
                                visible = !visible
                            }
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
            }

            demo("jsroot", "JSROOT cube"){
                jsRootGeometry {
                    y = 110.0
                    shape = box(50, 50, 50)
                    color(12285)
                }
            }
        }


    }

    override fun dispose() = emptyMap<String, Any>()//mapOf("lines" to presenter.dispose())
}