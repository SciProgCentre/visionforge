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
        require(bottomRadius > 0) { "Bottom radius must be positive" }
        require(topRadius > 0) { "Top radius must be positive" }
    }

    override fun <T : Any> toGeometry(geometryBuilder: GeometryBuilder<T>) {

        val segments: Int = detail ?: 32
        require(segments >= 4) { "The number of segments in cone segment is too small" }

        val angleStep = phi / (segments - 1)

        /**
         * Top and bottom shape
         */
        fun shape(r: Float, z: Float): List<Point3D> = (0 until segments).map { i ->
            Point3D(r * cos(phiStart + angleStep * i), r * sin(phiStart + angleStep * i), z)
        }

        with(geometryBuilder) {

            // top and bottom faces
            val bottomOuterPoints: List<Point3D> = shape(topRadius, -height / 2)
            val upperOuterPoints: List<Point3D> = shape(bottomRadius, height / 2)

            //outer face
            for (it in 1 until segments) {
                face4(bottomOuterPoints[it - 1], bottomOuterPoints[it], upperOuterPoints[it], upperOuterPoints[it - 1])
            }

            //if the cone is closed
            if (phi == PI2) {
                face4(bottomOuterPoints.last(), bottomOuterPoints[0], upperOuterPoints[0], upperOuterPoints.last())
            }

            //top and bottom cups
            val zeroBottom = Point3D(0f, 0f, -height / 2)
            val zeroTop = Point3D(0f, 0f, height / 2)
            for (it in 1 until segments) {
                face(bottomOuterPoints[it - 1], zeroBottom, bottomOuterPoints[it])
                face(upperOuterPoints[it - 1], upperOuterPoints[it], zeroTop)
            }
            // closed surface
            if (phi == PI2) {
                face(bottomOuterPoints.last(), zeroBottom, bottomOuterPoints[0])
                face(upperOuterPoints.last(), upperOuterPoints[0], zeroTop)
            } else {
                face4(zeroTop, zeroBottom, bottomOuterPoints[0], upperOuterPoints[0])
                face4(zeroTop, zeroBottom, bottomOuterPoints.last(), upperOuterPoints.last())
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