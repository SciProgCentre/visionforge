package space.kscience.visionforge.examples

import space.kscience.kmath.complex.Quaternion
import space.kscience.kmath.geometry.RotationOrder
import space.kscience.kmath.geometry.degrees
import space.kscience.kmath.geometry.fromEuler
import space.kscience.visionforge.html.ResourceLocation
import space.kscience.visionforge.solid.*
import kotlin.math.PI

fun main() = makeVisionFile(resourceLocation = ResourceLocation.SYSTEM) {

    val direction = Quaternion.fromEuler( 45.degrees, 45.degrees, 0.degrees, RotationOrder.XYZ)

    vision("canvas") {
        requirePlugin(Solids)
        solid {
            rotationX = -PI / 2
            rotationZ = PI
            axes(200)
            ambientLight()
            cylinder(50, 5, name = "base")
            solidGroup("frame") {
                rotationY = PI/2
                rotationX = -PI/2
                rotationZ = -PI/2
                z = 60
                axes(200)
                solidGroup("antenna") {
                    tube(40, 10, 30)
                    sphereLayer(100, 95, theta = PI / 6) {
                        z = 100
                        rotationX = -PI / 2
                    }
                    cylinder(5, 30) {
                        z = 15
                    }
                    this.quaternion = direction
                }
            }
        }
    }
}