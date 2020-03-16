@file:UseSerializers(Point3DSerializer::class)

package hep.dataforge.vis.spatial

import hep.dataforge.meta.Config
import hep.dataforge.vis.common.AbstractVisualObject
import hep.dataforge.vis.common.set
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Serializable
@SerialName("3d.sphere")
class Sphere(
    var radius: Float,
    var phiStart: Float = 0f,
    var phi: Float = PI2,
    var thetaStart: Float = 0f,
    var theta: Float = PI.toFloat()
) : AbstractVisualObject(), VisualObject3D, Shape {

    override var properties: Config? = null

    override var position: Point3D? = null
    override var rotation: Point3D? = null
    override var scale: Point3D? = null

    override fun <T : Any> toGeometry(geometryBuilder: GeometryBuilder<T>) {
        fun point3DfromSphCoord(r: Float, theta: Float, phi: Float): Point3D {
            // This transformation matches three.js sphere implementation
            val y = r * cos(theta)
            val z = r * sin(theta) * sin(phi)
            val x = - r * sin(theta) * cos(phi)
            return Point3D(x, y, z)
        }
        val segments = this.detail ?: 8
        require(segments >= 4) { "The detail for sphere must be >= 4" }
        val phiStep = phi / segments
        val thetaStep = theta / segments
        for (i in 0 until segments) {  // theta iteration
            val theta1 = thetaStart + i * thetaStep
            val theta2 = theta1 + thetaStep
            for (j in 0 until segments) {   // phi iteration
                val phi1 = phiStart + j * phiStep
                val phi2 = phi1 + phiStep
                val point1 = point3DfromSphCoord(radius, theta1, phi1)
                val point2 = point3DfromSphCoord(radius, theta1, phi2)
                val point3 = point3DfromSphCoord(radius, theta2, phi2)
                val point4 = point3DfromSphCoord(radius, theta2, phi1)
                geometryBuilder.apply {
                    // 1-2-3-4 gives the same face but with opposite orientation
                    face4(point1, point4, point3, point2)
                }
            }
        }
    }
}

inline fun VisualGroup3D.sphere(
    radius: Number,
    phi: Number = 2 * PI,
    theta: Number = PI,
    name: String = "",
    action: Sphere.() -> Unit = {}
) = Sphere(
    radius.toFloat(),
    phi = phi.toFloat(),
    theta = theta.toFloat()
).apply(action).also { set(name, it) }