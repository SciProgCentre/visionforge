package space.kscience.visionforge.examples

import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import space.kscience.dataforge.meta.configure
import space.kscience.kmath.complex.Quaternion
import space.kscience.kmath.complex.QuaternionField
import space.kscience.kmath.complex.conjugate
import space.kscience.kmath.geometry.*
import space.kscience.visionforge.solid.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

fun main() = serve {

    val azimuth = 60.degrees
    val inclination = 15.degrees

    val direction = with(QuaternionField) {
        Quaternion.fromRotation(-azimuth, Euclidean3DSpace.zAxis) *
                Quaternion.fromRotation(Angle.piDiv2 - inclination, Euclidean3DSpace.yAxis)
    }

    //val direction2 = Quaternion.fromEuler(Angle.zero, Angle.piDiv2 - inclination, -azimuth, RotationOrder.ZYX)

    val target = Quaternion.fromEuler((-45).degrees, 45.degrees, Angle.zero, RotationOrder.XYZ)


    vision("canvas") {
        requirePlugin(Solids)

        solid(options = {
            configure { "controls.enabled" put false }
        }) {
            rotationX = -PI / 2
            rotationZ = PI
            //axes(200)
            ambientLight()
            val platform = solidGroup("platform") {
                cylinder(50, 5, name = "base")
                solidGroup("frame") {
                    z = 60

                    val antenna = solidGroup("antenna") {
                        axes(200)
                        tube(40, 10, 30)
                        sphereLayer(100, 95, theta = PI / 6) {
                            z = 100
                            rotationX = -PI / 2
                        }
                        cylinder(5, 30) {
                            z = 15
                        }

                        sphereLayer(101, 94, phi = PI / 32, theta = PI / 6) {
                            z = 100
                            rotationX = -PI / 2
                            color("red")
                        }

                        quaternion = target
                    }
                }
            }

            val frame = platform["frame"] as SolidGroup

            val antenna = frame["antenna"] as SolidGroup

            val xPeriod = 5000 //ms
            val yPeriod = 7000 //ms

            val incRot = Quaternion.fromRotation(30.degrees, Euclidean3DSpace.zAxis)


            val rotationJob = context.launch {
                var time: Long = 0L
                while (isActive) {
                    with(QuaternionField) {
                        delay(200)
                        platform.quaternion = Quaternion.fromRotation(
                            15.degrees * sin(time.toDouble() * 2 * PI / xPeriod),
                            Euclidean3DSpace.xAxis
                        ) * Quaternion.fromRotation(
                            15.degrees * cos(time * 2 * PI / yPeriod),
                            Euclidean3DSpace.yAxis
                        )

                        val qi = platform.quaternion * incRot

                        antenna.quaternion = qi.conjugate * incRot.conjugate * target

                        time += 200
                        //antenna.quaternion = Quaternion.fromRotation(5.degrees, Euclidean3DSpace.zAxis) * antenna.quaternion
                    }
                }
            }
        }
    }
}