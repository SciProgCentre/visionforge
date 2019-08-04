package hep.dataforge.vis.spatial.demo

import hep.dataforge.context.ContextBuilder
import hep.dataforge.vis.common.Colors
import hep.dataforge.vis.hmr.ApplicationBase
import hep.dataforge.vis.hmr.startApplication
import hep.dataforge.vis.spatial.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random


private class ThreeDemoApp : ApplicationBase() {

    override val stateKeys: List<String> = emptyList()

    override fun start(state: Map<String, Any>) {

        //TODO replace by optimized builder after dataforge 0.1.3-dev-8
        val context = ContextBuilder("three-demo").build()

        context.plugins.load(ThreeDemoGrid()).run {
            demo("shapes", "Basic shapes") {
                box(100.0, 100.0, 100.0) {
                    z = 110.0
                }
                sphere(50.0) {
                    x = 110
                    detail = 16
                }
                tube(50, height = 10, innerRadius = 25, angle = PI) {
                    y = 110
                    detail = 16
                    rotationX = PI/4
                }
            }

            demo("dynamic", "Dynamic properties") {
                val group = group {
                    box(100, 100, 100) {
                        z = 110.0
                    }

                    box(100, 100, 100) {
                        visible = false
                        x = 110.0
                        //override color for this cube
                        color(1530)

                        GlobalScope.launch {
                            while (isActive) {
                                delay(500)
                                visible = !(visible ?: false)
                            }
                        }
                    }
                }

                GlobalScope.launch {
                    val random = Random(111)
                    while (isActive) {
                        delay(1000)
                        group.color(random.nextInt(0, Int.MAX_VALUE))
                    }
                }
            }

            demo("rotation", "Rotations") {
                box(100, 100, 100)
                group {
                    x = 200
                    rotationY = PI / 4
                    box(100, 100, 100) {
                        rotationZ = PI / 4
                        color(Colors.red)
                    }
                }
            }

            demo("extrude", "extruded shape") {
                extrude {
                    shape {
                        polygon(8, 50)
                    }
                    for (i in 0..100) {
                        layer(i * 5, 20 * sin(2 * PI / 100 * i), 20 * cos(2 * PI / 100 * i))
                    }
                    color(Colors.teal)
                }
            }

            demo("CSG", "CSG operations") {
                composite(CompositeType.UNION) {
                    box(100, 100, 100) {
                        z = 50
                    }
                    sphere(50)
                    material {
                        "color" to Colors.lightgreen
                        "opacity" to 0.3
                    }
                }
                composite(CompositeType.INTERSECT) {
                    y = 300
                    box(100, 100, 100) {
                        z = 50
                    }
                    sphere(50)
                    color(Colors.red)
                }
                composite(CompositeType.SUBTRACT) {
                    y = -300
                    box(100, 100, 100) {
                        z = 50
                    }
                    sphere(50)
                    color(Colors.blue)
                }
            }
        }


    }

    override fun dispose() = emptyMap<String, Any>()//mapOf("lines" to presenter.dispose())
}

fun main() {
    startApplication(::ThreeDemoApp)
}