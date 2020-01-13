@file:UseSerializers(Point3DSerializer::class)

package hep.dataforge.vis.spatial

import hep.dataforge.io.serialization.ConfigSerializer
import hep.dataforge.meta.Config
import hep.dataforge.vis.common.AbstractVisualObject
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlin.math.cos
import kotlin.math.sin

/**
 * A cylinder or cut cone segment
 */
@Serializable
class ConeSegment(
    var radius: Float,
    var height: Float,
    var upperRadius: Float,
    var startAngle: Float = 0f,
    var angle: Float = PI2
) : AbstractVisualObject(), VisualObject3D, Shape {

    @Serializable(ConfigSerializer::class)
    override var properties: Config? = null

    override var position: Point3D? = null
    override var rotation: Point3D? = null
    override var scale: Point3D? = null

    override fun <T : Any> toGeometry(geometryBuilder: GeometryBuilder<T>) {
        val segments = detail ?: 8
        require(segments >= 4) { "The number of segments in cone segment is too small" }
        val angleStep = angle / (segments - 1)

        fun shape(r: Float, z: Float): List<Point3D> {
            return (0 until segments).map { i ->
                Point3D(r * cos(startAngle + angleStep * i), r * sin(startAngle + angleStep * i), z)
            }
        }

        geometryBuilder.apply {

            //creating shape in x-y plane with z = 0
            val bottomOuterPoints = shape(upperRadius, -height / 2)
            val upperOuterPoints = shape(radius, height / 2)
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

inline fun VisualGroup3D.cylinder(
    r: Number,
    height: Number,
    name: String = "",
    block: ConeSegment.() -> Unit = {}
): ConeSegment = ConeSegment(
    r.toFloat(),
    height.toFloat(),
    r.toFloat()
).apply(block).also { set(name, it) }