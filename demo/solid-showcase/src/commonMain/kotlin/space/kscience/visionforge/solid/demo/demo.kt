package space.kscience.visionforge.solid.demo

import kotlinx.coroutines.*
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.invoke
import space.kscience.dataforge.names.Name
import space.kscience.kmath.geometry.Euclidean3DSpace
import space.kscience.kmath.geometry.radians
import space.kscience.visionforge.Colors
import space.kscience.visionforge.solid.*
import space.kscience.visionforge.solid.specifications.Canvas3DOptions
import space.kscience.visionforge.visible
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random


fun VisionLayout<Solid>.demo(name: String, title: String = name, block: SolidGroup.() -> Unit) {
    val meta = Meta {
        "title" put title
    }
    val vision = solids.solidGroup {
        block()
        ambientLight {
            color.set(Colors.white)
        }
    }
    render(Name.parse(name), vision, meta)
}

val canvasOptions = Canvas3DOptions {
    size {
        minSize = 400
    }
    axes {
        size = 500.0
        visible = true
    }
    camera {
        distance = 600.0
        latitude = PI / 6
    }
}

@OptIn(DelicateCoroutinesApi::class)
fun VisionLayout<Solid>.showcase() {
    demo("shapes", "Basic shapes") {
        ambientLight()
        box(100.0, 100.0, 100.0) {
            z = -110.0
            color.set("teal")
        }
        sphere(50.0) {
            x = 110
            detail = 16
            color.set("red")
        }
        tube(50, height = 10, innerRadius = 25, angle = PI) {
            y = 110
            detail = 16
            rotationX = PI / 4
            color.set("blue")
        }
        sphereLayer(50, 40, theta = PI / 2) {
            rotationX = -PI * 3 / 4
            z = 110
            color.set(Colors.pink)
        }
    }

    demo("dynamic", "Dynamic properties") {
        val group = solidGroup {
            box(100, 100, 100) {
                z = 110.0
                opacity = 0.5
            }

            box(100, 100, 100) {
                visible = false
                x = 110.0
                //override color for this cube
                color.set(1530)

                GlobalScope.launch(Dispatchers.Main) {
                    while (isActive) {
                        delay(500)
                        visible = !(visible ?: false)
                    }
                }
            }
        }

        GlobalScope.launch(Dispatchers.Main) {
            val random = Random(111)
            while (isActive) {
                delay(1000)
                group.color.set(random.nextInt(0, Int.MAX_VALUE))
            }
        }
    }

    demo("rotation", "Rotations") {
        box(100, 100, 100)
        solidGroup {
            x = 200
            rotationY = PI / 4
            axes(200)
            box(100, 100, 100) {
                rotate((PI / 4).radians, Euclidean3DSpace.zAxis)
                GlobalScope.launch(Dispatchers.Main) {
                    while (isActive) {
                        delay(100)
                        rotate((PI/20).radians,Euclidean3DSpace.yAxis)
                    }
                }
                color.set(Colors.red)
            }
        }
    }

    demo("extrude", "extruded shape") {
        extruded {
            shape {
                polygon(8, 50)
            }
            for (i in 0..100) {
                layer(i * 5, 20 * sin(2 * PI / 100 * i), 20 * cos(2 * PI / 100 * i))
            }
            color.set(Colors.teal)
            rotationX = -PI / 2
        }
    }

    demo("lines", "Track / line segments") {
        sphere(100) {
            detail = 32
            opacity = 0.4
            color.set(Colors.blue)
        }
        repeat(20) {
            polyline(
                Float32Vector3D(100, 100, 100),
                Float32Vector3D(-100, -100, -100)
            ) {
                thickness = 3.0
                rotationX = it * PI2 / 20
                color.set(Colors.green)
                //rotationY = it * PI2 / 20
            }
        }
    }

    demo("text", "Box with a label") {
        box(100, 100, 50) {
            opacity = 0.3
        }
        label("Hello, world!", fontSize = 12) {
            z = 26
        }
    }

    demo("STL", "STL loaded from URL") {
        stl("https://ozeki.hu/attachments/116/Menger_sponge_sample.stl")
    }
}

fun VisionLayout<Solid>.showcaseCSG() {
    demo("CSG.simple", "CSG operations") {
        composite(CompositeType.INTERSECT) {
            y = 300
            box(100, 100, 100) {
                z = 50
            }
            sphere(50) {
                detail = 32
            }
            material {
                color.set(Colors.pink)
            }
        }
        composite(CompositeType.UNION) {
            box(100, 100, 100) {
                z = 50
            }
            sphere(50) {
                detail = 32
            }
            color.set("lightgreen")
            opacity = 0.7
        }
        composite(CompositeType.SUBTRACT) {
            y = -300
            box(100, 100, 100) {
                z = 50
            }
            sphere(50) {
                detail = 32
            }
            color.set("teal")
            opacity = 0.7
        }
    }

    demo("CSG.custom", "CSG with manually created object") {
        intersect {
            cylinder(60, 10) {
                detail = 32
            }
            box(100, 100, 100)
            color.set("red")
            opacity = 0.5
        }
    }
}