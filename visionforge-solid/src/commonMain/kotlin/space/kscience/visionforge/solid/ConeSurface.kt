package space.kscience.visionforge.solid

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.visionforge.VisionBuilder
import space.kscience.visionforge.VisionContainerBuilder
import space.kscience.visionforge.set
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * A conical or cylindrical surface
 */
@Serializable
@SerialName("solid.coneSurface")
public class ConeSurface(
    public var bottomRadius: Float,
    public var bottomInnerRadius: Float,
    public var height: Float,
    public var topRadius: Float,
    public var topInnerRadius: Float,
    public var startAngle: Float = 0f,
    public var angle: Float = PI2,
) : SolidBase(), GeometrySolid {

    init {
        require(bottomRadius > 0) { "Cone surface bottom radius must be positive" }
        require(height > 0) { "Cone surface height must be positive" }
        require(bottomInnerRadius >= 0) { "Cone surface bottom inner radius must be non-negative" }
        //require(startAngle >= 0)
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
            val bottomOuterPoints = shape(bottomRadius, -height / 2)
            val topOuterPoints = shape(topRadius, height / 2)
            //outer face
            (1 until segments).forEach {
                face4(bottomOuterPoints[it - 1], bottomOuterPoints[it], topOuterPoints[it], topOuterPoints[it - 1])
            }

            if (angle == PI2) {
                face4(bottomOuterPoints.last(), bottomOuterPoints[0], topOuterPoints[0], topOuterPoints.last())
            }
            if (bottomInnerRadius == 0f) {
                val zeroBottom = Point3D(0f, 0f, 0f)
                val zeroTop = Point3D(0f, 0f, height)
                (1 until segments).forEach {
                    face(bottomOuterPoints[it - 1], zeroBottom, bottomOuterPoints[it])
                    face(topOuterPoints[it - 1], topOuterPoints[it], zeroTop)
                }
                if (angle == PI2) {
                    face(bottomOuterPoints.last(), zeroBottom, bottomOuterPoints[0])
                    face(topOuterPoints.last(), topOuterPoints[0], zeroTop)
                } else {
                    face4(zeroTop, zeroBottom, bottomOuterPoints[0], topOuterPoints[0])
                    face4(zeroTop, zeroBottom, bottomOuterPoints.last(), topOuterPoints.last())
                }
            } else {
                val bottomInnerPoints = shape(bottomInnerRadius, -height / 2)
                val topInnerPoints = shape(topInnerRadius, height / 2)
                //outer face
                (1 until segments).forEach {
                    // inner surface
                    face4(
                        bottomInnerPoints[it],
                        bottomInnerPoints[it - 1],
                        topInnerPoints[it - 1],
                        topInnerPoints[it]
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
                        topInnerPoints[it],
                        topInnerPoints[it - 1],
                        topOuterPoints[it - 1],
                        topOuterPoints[it]
                    )
                }
                if (angle == PI2) {
                    face4(bottomInnerPoints[0], bottomInnerPoints.last(), topInnerPoints.last(), topInnerPoints[0])
                    face4(
                        bottomInnerPoints.last(),
                        bottomInnerPoints[0],
                        bottomOuterPoints[0],
                        bottomOuterPoints.last()
                    )
                    face4(topInnerPoints[0], topInnerPoints.last(), topOuterPoints.last(), topOuterPoints[0])
                } else {
                    face4(bottomInnerPoints[0], bottomOuterPoints[0], topOuterPoints[0], topInnerPoints[0])
                    face4(
                        bottomOuterPoints.last(),
                        bottomInnerPoints.last(),
                        topInnerPoints.last(),
                        topOuterPoints.last()
                    )
                }
            }
        }
    }
}


@VisionBuilder
public inline fun VisionContainerBuilder<Solid>.tube(
    radius: Number,
    height: Number,
    innerRadius: Number,
    startAngle: Number = 0f,
    angle: Number = 2 * PI,
    name: String? = null,
    block: ConeSurface.() -> Unit = {},
): ConeSurface = ConeSurface(
    bottomRadius = radius.toFloat(),
    bottomInnerRadius = innerRadius.toFloat(),
    height = height.toFloat(),
    topRadius = radius.toFloat(),
    topInnerRadius = innerRadius.toFloat(),
    startAngle = startAngle.toFloat(),
    angle = angle.toFloat()
).apply(block).also { set(name, it) }

@VisionBuilder
public inline fun VisionContainerBuilder<Solid>.coneSurface(
    bottomOuterRadius: Number,
    bottomInnerRadius: Number,
    height: Number,
    topOuterRadius: Number,
    topInnerRadius: Number,
    name: String? = null,
    block: ConeSurface.() -> Unit = {},
): ConeSurface = ConeSurface(
    bottomRadius = bottomOuterRadius.toFloat(),
    bottomInnerRadius = bottomInnerRadius.toFloat(),
    height = height.toFloat(),
    topRadius = topOuterRadius.toFloat(),
    topInnerRadius = topInnerRadius.toFloat(),
).apply(block).also { set(name, it) }