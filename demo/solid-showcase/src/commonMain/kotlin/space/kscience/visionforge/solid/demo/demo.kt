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
            color(Colors.white)
            intensity = 0.5
        }
        pointLight(0, 0, 1000) {
            color(Colors.white)
            intensity = 10.0
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
            color("teal")
        }
        sphere(50.0) {
            x = 110
            detail = 16
            color("red")
        }
        tube(50, height = 10, innerRadius = 25, angle = PI) {
            y = 110
            detail = 16
            rotationX = PI / 4
            color("blue")
        }
        sphereLayer(50, 40, theta = PI / 2) {
            rotationX = -PI * 3 / 4
            z = 110
            color(Colors.pink)
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
                color(1530)

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
                group.color(random.nextInt(0, Int.MAX_VALUE))
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
                        rotate((PI / 20).radians, Euclidean3DSpace.yAxis)
                    }
                }
                color(Colors.red)
            }
        }
    }

    demo("extrude", "extruded shape") {
        extruded {
            shape {
                polygon(32, 50)
            }
            for (i in 0..100) {
                layer(i * 5, 20 * sin(2 * PI / 100 * i), 20 * cos(2 * PI / 100 * i))
            }
            rotationY = -PI / 2
            material {
                type = "lambert"
                color(Colors.teal)
            }
        }
    }

    demo("lines", "Track / line segments") {
        sphere(100) {
            detail = 32
            opacity = 0.4
            color(Colors.blue)
        }
        repeat(20) {
            polyline(
                Float32Vector3D(100, 100, 100),
                Float32Vector3D(-100, -100, -100)
            ) {
                thickness = 3.0
                rotationX = it * PI2 / 20
                color(Colors.green)
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
        stl("Menger_sponge_sample.stl") {
            scale(100f)
            material {
                type = "phong"
                color("red")
                specularColor("blue")
            }
        }
        //stl("https://ozeki.hu/attachments/116/Menger_sponge_sample.stl")
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
                color(Colors.pink)
            }
        }
        composite(CompositeType.UNION) {
            box(100, 100, 100) {
                z = 50
            }
            sphere(50) {
                detail = 32
            }
            color("lightgreen")
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
            color("teal")
            opacity = 0.7
        }
    }

    demo("CSG.custom", "CSG with manually created object") {
        intersect {
            cylinder(60, 10) {
                detail = 32
            }
            box(100, 100, 100)
            color("red")
            opacity = 0.5
        }
    }
}