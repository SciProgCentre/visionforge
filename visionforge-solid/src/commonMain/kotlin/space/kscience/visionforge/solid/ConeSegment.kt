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
    public val startAngle: Float = 0f,
    public val angle: Float = PI2
) : SolidBase<ConeSegment>(), GeometrySolid {

    override fun <T : Any> toGeometry(geometryBuilder: GeometryBuilder<T>) {
        val segments = detail ?: 32
        require(segments >= 4) { "The number of segments in cone segment is too small" }
        val angleStep = angle / (segments - 1)

        fun shape(r: Float, z: Float): List<Point3D> {
            return (0 until segments).map { i ->
                Point3D(r * cos(startAngle + angleStep * i), r * sin(startAngle + angleStep * i), z)
            }
        }

        geometryBuilder.apply {

            //creating shape in x-y plane with z = 0
            val bottomOuterPoints = shape(topRadius, -height / 2)
            val upperOuterPoints = shape(bottomRadius, height / 2)
            //outer face
            (1 until segments).forEach {
                face4(bottomOuterPoints[it - 1], bottomOuterPoints[it], upperOuterPoints[it], upperOuterPoints[it - 1])
            }

            if (angle == PI2) {
                face4(bottomOuterPoints.last(), bottomOuterPoints[0], upperOuterPoints[0], upperOuterPoints.last())
            }

            val zeroBottom = Point3D(0f, 0f, 0f)
            val zeroTop = Point3D(0f, 0f, height)
            (1 until segments).forEach {
                face(bottomOuterPoints[it - 1], zeroBottom, bottomOuterPoints[it])
                face(upperOuterPoints[it - 1], upperOuterPoints[it], zeroTop)
            }
            if (angle == PI2) {
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
    block: ConeSegment.() -> Unit = {}
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
    block: ConeSegment.() -> Unit = {}
): ConeSegment = ConeSegment(
    bottomRadius.toFloat(),
    height.toFloat(),
    topRadius = upperRadius.toFloat(),
    startAngle = startAngle.toFloat(),
    angle = angle.toFloat()
).apply(block).also { setChild(name, it) }