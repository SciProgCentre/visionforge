package space.kscience.visionforge.examples

import kotlin.math.PI
import space.kscience.visionforge.Colors
import space.kscience.visionforge.solid.*

fun main() = makeVisionFile{
    vision("canvas") {
        solid {
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


            tube(30,20, 20){
                detail = 31
                y = - 220
            }
        }
    }
}