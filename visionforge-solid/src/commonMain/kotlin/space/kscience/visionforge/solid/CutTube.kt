package space.kscience.visionforge.solid

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.visionforge.MutableVisionContainer
import space.kscience.visionforge.VisionBuilder
import space.kscience.visionforge.setChild
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * A tube section cut with two planes.
 * The centers of the two sections are positioned
 * along the central axis of the tube, at the top
 * and at the bottom.
 *
 * The default segment number is 32.
 */
@Serializable
@SerialName("solid.cutTube")
public class CutTube(
    public val outerRadius: Float,
    public val innerRadius: Float,
    public val height: Float,
    public val phiStart: Float = 0f,
    public val phi: Float = PI2,
    public val nTop: Float32Vector3D,
    public val nBottom: Float32Vector3D,
) : SolidBase<CutTube>(), GeometrySolid {

    init {
        require(innerRadius >= 0) { "Tube inner radius must be non-negative" }
        require(outerRadius >= 0) { "Tube outer radius must not be less than its inner radius" }
        require(height >= 0) { "Tube height must be non-negative" }
        //TODO implement more "subtle" section checks
        require(abs(nTop.z) > 1e-5) { "Top section is almost vertical" }
        require(abs(nBottom.z) > 1e-5) { "Bottom section is almost vertical" }
        //require(phiStart >= 0)
        require(phi in (0f..(PI2)))
    }

    override fun <T : Any> toGeometry(geometryBuilder: GeometryBuilder<T>) {
        val segments = detail ?: 32
        require(segments >= 4) { "The number of segments in the tube is too small" }
        val angleStep = phi / (segments - 1)

        fun section(r: Float, z: Float, n: Float32Vector3D): List<Float32Vector3D> = (0 until segments).map { i ->
            val x = r * cos(phiStart + angleStep * i)
            val y = r * sin(phiStart + angleStep * i)
            Float32Vector3D(x, y, (n.z * z - n.x * x - n.y * y) / n.z)
        }

        geometryBuilder.apply {
            //creating top and bottom sections of the tube:
            //shapes in planes defined with (0, 0, +-z) points and normal vectors
            //TODO check for an intersection of the sections
            val topOuterPoints = section(outerRadius, height / 2, nTop)
            val bottomOuterPoints = section(outerRadius, -height / 2, nBottom)


            //outer face
            for (it in 1 until segments) {
                face4(bottomOuterPoints[it - 1], bottomOuterPoints[it], topOuterPoints[it], topOuterPoints[it - 1])
            }
            if (phi == PI2) {
                face4(bottomOuterPoints.last(), bottomOuterPoints[0], topOuterPoints[0], topOuterPoints.last())
            }

            if (innerRadius == 0f) {
                val zeroBottom = Float32Vector3D(0f, 0f, -height / 2)
                val zeroTop = Float32Vector3D(0f, 0f, height / 2)
                (1 until segments).forEach {
                    face(bottomOuterPoints[it - 1], zeroBottom, bottomOuterPoints[it])
                    face(topOuterPoints[it - 1], topOuterPoints[it], zeroTop)
                }
                if (phi == PI2) {
                    face(bottomOuterPoints.last(), zeroBottom, bottomOuterPoints[0])
                    face(topOuterPoints.last(), topOuterPoints[0], zeroTop)
                } else {
                    face4(zeroTop, zeroBottom, bottomOuterPoints[0], topOuterPoints[0])
                    face4(zeroTop, zeroBottom, bottomOuterPoints.last(), topOuterPoints.last())
                }
            } else {
                val topInnerPoints = section(innerRadius, height / 2, nTop)
                val bottomInnerPoints = section(innerRadius, -height / 2, nBottom)
                // inner face
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
                if (phi == PI2) {
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
public inline fun MutableVisionContainer<Solid>.cutTube(
    outerRadius: Number,
    innerRadius: Number,
    height: Number,
    startAngle: Number = 0f,
    angle: Number = PI2,
    topNormal: Float32Vector3D,
    bottomNormal: Float32Vector3D,
    name: String? = null,
    block: CutTube.() -> Unit = {},
): CutTube = CutTube(
    outerRadius = outerRadius.toFloat(),
    innerRadius = innerRadius.toFloat(),
    height = height.toFloat(),
    phiStart = startAngle.toFloat(),
    phi = angle.toFloat(),
    nTop = topNormal,
    nBottom = bottomNormal
).apply(block).also { setChild(name, it) }
