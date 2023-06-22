package space.kscience.visionforge.examples

import space.kscience.kmath.complex.Quaternion
import space.kscience.kmath.complex.QuaternionField
import space.kscience.kmath.geometry.Angle
import space.kscience.kmath.geometry.Euclidean3DSpace
import space.kscience.kmath.geometry.degrees
import space.kscience.kmath.geometry.fromRotation
import space.kscience.visionforge.html.ResourceLocation
import space.kscience.visionforge.solid.*
import kotlin.math.PI

fun main() = makeVisionFile(resourceLocation = ResourceLocation.SYSTEM) {

    val azimuth = 60.degrees
    val inclination = 15.degrees

    val direction = with(QuaternionField) {
        Quaternion.fromRotation(-azimuth, Euclidean3DSpace.zAxis) *
                Quaternion.fromRotation(Angle.piDiv2 - inclination, Euclidean3DSpace.yAxis)
    }

    //val direction2 = Quaternion.fromEuler(Angle.zero, Angle.piDiv2 - inclination, -azimuth, RotationOrder.ZYX)


    vision("canvas") {
        requirePlugin(Solids)
        solid {
            rotationX = -PI / 2
            rotationZ = PI
            axes(200)
            ambientLight()
            cylinder(50, 5, name = "base")
            solidGroup("frame") {
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
                    quaternion = direction
                }
            }
        }
    }
}