package hep.dataforge.vis.spatial.demo

import hep.dataforge.vis.common.Colors
import hep.dataforge.vis.spatial.*
import javafx.stage.Stage
import kotlinx.coroutines.*
import kotlinx.coroutines.javafx.JavaFx
import tornadofx.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class FXDemoApp : App(FXDemoGrid::class) {

    val view: FXDemoGrid by inject()

    override fun start(stage: Stage) {
        super.start(stage)
        view.run {
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
                    rotationX = PI / 4
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

                        GlobalScope.launch(Dispatchers.JavaFx) {
                            while (isActive) {
                                delay(500)
                                visible = !(visible ?: false)
                            }
                        }
                    }
                }

                GlobalScope.launch(Dispatchers.JavaFx) {
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

//            demo("CSG.simple", "CSG operations") {
//                composite(CompositeType.UNION) {
//                    box(100, 100, 100) {
//                        z = 50
//                    }
//                    sphere(50)
//                    material {
//                        color(Colors.lightgreen)
//                        opacity = 0.3f
//                    }
//                }
//                composite(CompositeType.INTERSECT) {
//                    y = 300
//                    box(100, 100, 100) {
//                        z = 50
//                    }
//                    sphere(50)
//                    color(Colors.red)
//                }
//                composite(CompositeType.SUBTRACT) {
//                    y = -300
//                    box(100, 100, 100) {
//                        z = 50
//                    }
//                    sphere(50)
//                    color(Colors.blue)
//                }
//            }

//            demo("CSG.custom", "CSG with manually created object") {
//                intersect {
//                    box(100, 100, 100)
//                    tube(60, 10) {
//                        detail = 180
//                    }
//                }
//            }

            demo("lines", "Track / line segments") {
                sphere(100) {
                    color(Colors.blue)
                    detail = 50
                    opacity = 0.4
                }
                repeat(20) {
                    polyline(Point3D(100, 100, 100), Point3D(-100, -100, -100)) {
                        thickness = 208.0
                        rotationX = it * PI2 / 20
                        color(Colors.green)
                        //rotationY = it * PI2 / 20
                    }
                }
            }

//            demo("dynamicBox", "Dancing boxes") {
//                val boxes = (-10..10).flatMap { i ->
//                    (-10..10).map { j ->
//                        varBox(10, 10, 0, name = "cell_${i}_${j}") {
//                            x = i * 10
//                            y = j * 10
//                            value = 128
//                            setProperty(EDGES_ENABLED_KEY, false)
//                            setProperty(WIREFRAME_ENABLED_KEY, false)
//                        }
//                    }
//                }
//                GlobalScope.launch {
//                    while (isActive) {
//                        delay(200)
//                        boxes.forEach { box ->
//                            box.value = (box.value + Random.nextInt(-15, 15)).coerceIn(0..255)
//                        }
//                    }
//                }
//            }

        }
    }
}

//class SpatialDemoView : View() {
//    private val plugin = Global.plugins.fetch(FX3DPlugin)
//    private val canvas = FXCanvas3D(plugin)
//
//    override val root: Parent = borderpane {
//        center = canvas.root
//    }
//
//    lateinit var group: VisualGroup3D
//
//    init {
//        canvas.render {
//            group = group {
//                box(100, 100, 100)
//                box(100, 100, 100) {
//                    x = 110.0
//                    color(Colors.blue)
//                }
//            }
//        }
//
//        //var color by group.config.number(1530)
//
//        GlobalScope.launch {
//            val random = Random(111)
//            while (isActive) {
//                delay(1000)
//                group.color(random.nextInt(0, Int.MAX_VALUE))
//            }
//        }
//
////        canvas.apply {
////            angleY = -30.0
////            angleX = -15.0
////        }
//    }
//}


fun main() {
    launch<FXDemoApp>()
}