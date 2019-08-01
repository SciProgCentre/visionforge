package hep.dataforge.vis.spatial

import hep.dataforge.vis.common.VisualObject
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Stright tube segment
 */
class Tube(
    parent: VisualObject?,
    var radius: Float,
    var height: Float,
    var innerRadius: Float = 0f,
    var startAngle: Float = 0f,
    var angle: Float = PI2
) : VisualLeaf3D(parent), Shape {

    init {
        require(radius > 0)
        require(height > 0)
        require(innerRadius >= 0)
        require(startAngle >= 0)
        require(angle in (0f..(PI2)))
    }

    override fun <T : Any> toGeometry(geometryBuilder: GeometryBuilder<T>) {
        val segments = detail ?: 8
        require(segments >= 4) { "The number of segments in tube is too small" }
        val angleStep = angle / (segments - 1)

        fun shape(r: Float, z: Float): List<Point3D> {
            return (0 until segments).map { i ->
                Point3D(r * cos(startAngle + angleStep * i), r * sin(startAngle + angleStep * i), z)
            }
        }

        geometryBuilder.apply {

            //creating shape in x-y plane with z = 0
            val bottomOuterPoints = shape(radius, 0f)
            val upperOuterPoints = shape(radius, height)
            //outer face
            (1 until segments).forEach {
                face4(bottomOuterPoints[it - 1], bottomOuterPoints[it], upperOuterPoints[it], upperOuterPoints[it - 1])
            }

            if (angle == PI2) {
                face4(bottomOuterPoints.last(), bottomOuterPoints[0], upperOuterPoints[0], upperOuterPoints.last())
            }
            if (innerRadius == 0f) {
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
            } else {
                val bottomInnerPoints = shape(innerRadius, 0f)
                val upperInnerPoints = shape(innerRadius, height)
                //outer face
                (1 until segments).forEach {
                    // inner surface
                    face4(
                        bottomInnerPoints[it],
                        bottomInnerPoints[it - 1],
                        upperInnerPoints[it - 1],
                        upperInnerPoints[it]
                    )
                    //bottom cup
                    face4(
                        bottomInnerPoints[it - 1],
                        bottomInnerPoints[it],
                        bottomOuterPoints[it],
                        bottomOuterPoints[it - 1]
                    )
                    //upper cup
                    face4(
                        upperInnerPoints[it],
                        upperInnerPoints[it - 1],
                        upperOuterPoints[it - 1],
                        upperOuterPoints[it]
                    )
                }
                if (angle == PI2) {
                    face4(bottomInnerPoints[0], bottomInnerPoints.last(), upperInnerPoints.last(), upperInnerPoints[0])
                    face4(
                        bottomInnerPoints.last(),
                        bottomInnerPoints[0],
                        bottomOuterPoints[0],
                        bottomOuterPoints.last()
                    )
                    face4(upperInnerPoints[0], upperInnerPoints.last(), upperOuterPoints.last(), upperOuterPoints[0])
                } else{
                    face4(bottomInnerPoints[0],bottomOuterPoints[0],upperOuterPoints[0],upperInnerPoints[0])
                    face4(bottomOuterPoints.last(),bottomInnerPoints.last(),upperInnerPoints.last(),upperOuterPoints.last())
                }
            }
        }
    }
}

fun VisualGroup3D.tube(
    r: Number,
    height: Number,
    innerRadius: Number = 0f,
    startAngle: Number = 0f,
    angle: Number = 2 * PI,
    name: String? = null,
    block: Tube.() -> Unit = {}
): Tube {
    val tube = Tube(
        this,
        r.toFloat(),
        height.toFloat(),
        innerRadius.toFloat(),
        startAngle.toFloat(),
        angle.toFloat()
    ).apply(
        block
    )
    return tube.also { set(name, it) }
}