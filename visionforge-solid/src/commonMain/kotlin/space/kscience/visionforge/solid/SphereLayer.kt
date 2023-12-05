package space.kscience.visionforge.solid

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.visionforge.MutableVisionContainer
import space.kscience.visionforge.VisionBuilder
import space.kscience.visionforge.setChild
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * A spherical layer
 */
@Serializable
@SerialName("solid.sphereLayer")
public class SphereLayer(
    public val outerRadius: Float,
    public val innerRadius: Float,
    public val phiStart: Float = 0f,
    public val phi: Float = PI2,
    public val thetaStart: Float = 0f,
    public val theta: Float = PI.toFloat(),
) : SolidBase<SphereLayer>(), GeometrySolid {

    override fun <T : Any> toGeometry(geometryBuilder: GeometryBuilder<T>): Unit = geometryBuilder.run {
        require(outerRadius > 0) { "Outer radius must be positive" }
        require(innerRadius >= 0) { "inner radius must be non-negative" }

        fun point3dFromSphCoord(r: Float, theta: Float, phi: Float): Float32Vector3D {
            // This transformation matches three.js sphere implementation
            val y = r * cos(theta)
            val z = r * sin(theta) * sin(phi)
            val x = -r * sin(theta) * cos(phi)
            return Float32Vector3D(x, y, z)
        }

        val segments = detail ?: 32
        require(segments >= 4) { "The detail for sphere must be >= 4" }
        val phiStep = phi / segments
        val thetaStep = theta / segments
        for (i in 0 until segments) {  // theta iteration
            val theta1 = thetaStart + i * thetaStep
            val theta2 = theta1 + thetaStep
            for (j in 0 until segments) {   // phi iteration
                val phi1 = phiStart + j * phiStep
                val phi2 = phi1 + phiStep
                //outer points
                val outerPoint1 = point3dFromSphCoord(outerRadius, theta1, phi1)
                val outerPoint2 = point3dFromSphCoord(outerRadius, theta1, phi2)
                val outerPoint3 = point3dFromSphCoord(outerRadius, theta2, phi2)
                val outerPoint4 = point3dFromSphCoord(outerRadius, theta2, phi1)
                // 1-2-3-4 gives the same face but with opposite orientation
                face4(outerPoint1, outerPoint4, outerPoint3, outerPoint2)
                if (innerRadius > 0) {
                    val innerPoint1 = point3dFromSphCoord(innerRadius, theta1, phi1)
                    val innerPoint2 = point3dFromSphCoord(innerRadius, theta1, phi2)
                    val innerPoint3 = point3dFromSphCoord(innerRadius, theta2, phi2)
                    val innerPoint4 = point3dFromSphCoord(innerRadius, theta2, phi1)
                    face4(innerPoint1, innerPoint2, innerPoint3, innerPoint4)
                    //the cup
                    if (i == segments - 1 && theta != PI.toFloat() && innerRadius != outerRadius) {
                        face4(outerPoint4, innerPoint4, innerPoint3, outerPoint3)
                    }
                }
            }
        }
    }
}

@VisionBuilder
public inline fun MutableVisionContainer<Solid>.sphereLayer(
    outerRadius: Number,
    innerRadius: Number,
    phiStart: Number = 0f,
    phi: Number = PI2,
    thetaStart: Number = 0f,
    theta: Number = PI.toFloat(),
    name: String? = null,
    action: SphereLayer.() -> Unit = {},
): SphereLayer = SphereLayer(
    outerRadius.toFloat(),
    innerRadius.toFloat(),
    phiStart.toFloat(),
    phi.toFloat(),
    thetaStart.toFloat(),
    theta.toFloat()
).apply(action).also { setChild(name, it) }