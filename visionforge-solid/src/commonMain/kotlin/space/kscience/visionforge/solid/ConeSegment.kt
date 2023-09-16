package space.kscience.visionforge.solid

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.visionforge.MutableVisionContainer
import space.kscience.visionforge.VisionBuilder
import space.kscience.visionforge.setChild
import kotlin.math.cos
import kotlin.math.sin

/**
 * A solid cylinder or cut cone segment
 * The default segment number is 32
 */
@Serializable
@SerialName("solid.cone")
public class ConeSegment(
    public val bottomRadius: Float,
    public val height: Float,
    public val topRadius: Float,
    public val phiStart: Float = 0f,
    public val phi: Float = PI2,
) : SolidBase<ConeSegment>(), GeometrySolid {


    init {
        require(bottomRadius > 0) { "Cone segment bottom radius must be positive" }
        require(height > 0) { "Cone segment height must be positive" }
        require(topRadius >= 0) { "Cone segment top radius must be non-negative" }
        //require(startAngle >= 0)
        require(phi in (0f..(PI2)))
    }

    override fun <T : Any> toGeometry(geometryBuilder: GeometryBuilder<T>) {
        val segments = detail ?: 32
        require(segments >= 4) { "The number of segments in cone is too small" }
        val angleStep = phi / (segments - 1)

        fun shape(r: Float, z: Float): List<Float32Vector3D> = (0 until segments).map { i ->
            Float32Vector3D(r * cos(phiStart + angleStep * i), r * sin(phiStart + angleStep * i), z)
        }

        geometryBuilder.apply {

            //creating shape in x-y plane with z = 0
            val bottomPoints = shape(bottomRadius, -height / 2)
            val topPoints = shape(topRadius, height / 2)
            //outer face
            for (it in 1 until segments) {
                face4(bottomPoints[it - 1], bottomPoints[it], topPoints[it], topPoints[it - 1])
            }

            if (phi == PI2) {
                face4(bottomPoints.last(), bottomPoints[0], topPoints[0], topPoints.last())
            }
            val zeroBottom = Float32Vector3D(0f, 0f, -height / 2)
            val zeroTop = Float32Vector3D(0f, 0f, +height / 2)
            for (it in 1 until segments) {
                face(bottomPoints[it - 1], zeroBottom, bottomPoints[it])
                face(topPoints[it - 1], topPoints[it], zeroTop)
            }
            if (phi == PI2) {
                face(bottomPoints.last(), zeroBottom, bottomPoints[0])
                face(topPoints.last(), topPoints[0], zeroTop)
            } else {
                face4(zeroTop, zeroBottom, bottomPoints[0], topPoints[0])
                face4(zeroTop, zeroBottom, bottomPoints.last(), topPoints.last())
            }

        }
    }
}

@VisionBuilder
public inline fun MutableVisionContainer<Solid>.cylinder(
    r: Number,
    height: Number,
    name: String? = null,
    block: ConeSegment.() -> Unit = {},
): ConeSegment = ConeSegment(
    r.toFloat(),
    height.toFloat(),
    r.toFloat()
).apply(block).also { setChild(name, it) }

@VisionBuilder
public inline fun MutableVisionContainer<Solid>.cone(
    bottomRadius: Number,
    height: Number,
    upperRadius: Number = 0.0,
    startAngle: Number = 0f,
    angle: Number = PI2,
    name: String? = null,
    block: ConeSegment.() -> Unit = {},
): ConeSegment = ConeSegment(
    bottomRadius.toFloat(),
    height.toFloat(),
    topRadius = upperRadius.toFloat(),
    phiStart = startAngle.toFloat(),
    phi = angle.toFloat()
).apply(block).also { setChild(name, it) }