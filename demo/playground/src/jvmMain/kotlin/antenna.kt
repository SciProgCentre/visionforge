package space.kscience.visionforge.examples

import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import space.kscience.dataforge.meta.configure
import space.kscience.kmath.complex.Quaternion
import space.kscience.kmath.complex.QuaternionAlgebra
import space.kscience.kmath.complex.conjugate
import space.kscience.kmath.geometry.Angle
import space.kscience.kmath.geometry.degrees
import space.kscience.kmath.geometry.euclidean3d.Float64Space3D
import space.kscience.kmath.geometry.euclidean3d.RotationOrder
import space.kscience.kmath.geometry.euclidean3d.fromEuler
import space.kscience.kmath.geometry.euclidean3d.fromRotation
import space.kscience.visionforge.Colors
import space.kscience.visionforge.html.vision
import space.kscience.visionforge.solid.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

suspend fun main() = serve(
    routeConfiguration = {
        updateInterval = 100
    }
) {

//    val azimuth = 60.degrees
//    val inclination = 15.degrees

//    val direction = with(QuaternionField) {
//        Quaternion.fromRotation(-azimuth, Euclidean3DSpace.zAxis) *
//                Quaternion.fromRotation(Angle.piDiv2 - inclination, Euclidean3DSpace.yAxis)
//    }

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
            ambientLight {
                color(Colors.white)
                intensity = 3.0
            }
            val platform = solidGroup("platform") {
                cylinder(50, 5, name = "base")
                solidGroup("frame") {
                    z = 60

                    solidGroup("antenna") {
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

            val incRot = Quaternion.fromRotation(30.degrees, Float64Space3D.zAxis)


            context.launch {
                var time: Long = 0L
                while (isActive) {
                    with(QuaternionAlgebra) {
                        delay(100)
                        platform.quaternion = Quaternion.fromRotation(
                            15.degrees * sin(time.toDouble() * 2 * PI / xPeriod),
                            Float64Space3D.xAxis
                        ) * Quaternion.fromRotation(
                            15.degrees * cos(time * 2 * PI / yPeriod),
                            Float64Space3D.yAxis
                        )

                        val qi = platform.quaternion * incRot

                        antenna.quaternion = qi.conjugate * incRot.conjugate * target

                        time += 100
                        //antenna.quaternion = Quaternion.fromRotation(5.degrees, Euclidean3DSpace.zAxis) * antenna.quaternion
                    }
                }
            }
        }
    }
}