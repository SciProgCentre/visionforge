package hep.dataforge.vis.spatial.demo

import hep.dataforge.context.ContextBuilder
import hep.dataforge.meta.number
import hep.dataforge.vis.common.Colors
import hep.dataforge.vis.spatial.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random


class ThreeDemoApp : ApplicationBase() {

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
                    detail = 200
                }
            }

            demo("dynamic", "Dynamic properties") {
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
                        //override color for this cube
                        color(1530)

                        GlobalScope.launch {
                            while (isActive) {
                                delay(500)
                                visible = !visible
                            }
                        }
                    }
                }

                var material by group.properties.number(1530).int

                GlobalScope.launch {
                    val random = Random(111)
                    while (isActive) {
                        delay(1000)
                        material = random.nextInt(0, Int.MAX_VALUE)
                    }
                }
            }

//            demo("jsroot", "JSROOT cube") {
//                jsRootGeometry {
//                    y = 110.0
//                    shape = box(50, 50, 50)
//                    color(Colors.lightcoral)
//                    rotationX = PI / 4
//                    rotationY = PI / 4
//                }
//            }

            demo("extrude", "extruded shape") {
                extrude {
                    shape {
                        polygon(8, 50)
                    }
                    for (i in 0..100) {
                        layer(i * 5, 20 * sin(2 * PI / 100 * i), 20 * cos(2 * PI / 100 * i))
                    }
                }

                color(Colors.teal)
            }

            demo("CSG", "CSG operations") {
                composite(CompositeType.UNION) {
                    box(100, 100, 100) {
                        z = 100
                        rotationX = PI / 4
                        rotationY = PI / 4
                    }
                    box(100, 100, 100)
                    color {
                        "color" to Colors.lightgreen
                        "opacity" to 0.3
                    }
                }
                composite(CompositeType.INTERSECT) {
                    box(100, 100, 100) {
                        z = 100
                        rotationX = PI / 4
                        rotationY = PI / 4
                    }
                    box(100, 100, 100)
                    y = 300
                    color(Colors.red)
                }
                composite(CompositeType.SUBTRACT) {
                    box(100, 100, 100) {
                        z = 100
                        rotationX = PI / 4
                        rotationY = PI / 4
                    }
                    box(100, 100, 100)
                    y = -300
                    color(Colors.blue)
                }
            }
        }


    }

    override fun dispose() = emptyMap<String, Any>()//mapOf("lines" to presenter.dispose())
}