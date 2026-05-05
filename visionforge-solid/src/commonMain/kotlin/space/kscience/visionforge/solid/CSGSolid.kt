package space.kscience.visionforge.solid

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.dataforge.meta.Meta
import space.kscience.kmath.geometry.euclidean3d.Float32Space3D
import space.kscience.kmath.geometry.euclidean3d.Float32Vector3D
import space.kscience.kmath.geometry.euclidean3d.Float64Space3D
import space.kscience.kmath.geometry.euclidean3d.Float64Vector3D
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

public enum class CSGOperation {
    UNION, SUBTRACT, INTERSECT
}

private fun triangulatePolygon(p: CSGPolygon): List<CSGPolygon> {
    if (p.vertices.size == 3) return listOf(p)
    val out = mutableListOf<CSGPolygon>()
    val v0 = p.vertices[0]
    for (i in 1 until p.vertices.size - 1) {
        out += CSGPolygon(listOf(v0, p.vertices[i], p.vertices[i + 1]), p.shared)
    }
    return out
}

@Serializable
@SerialName("solid.csg")
public class CSGSolid(
    @Serializable private val polygonData: List<CSGPolygonData> = emptyList()
) : SolidBase<CSGSolid>(), GeometrySolid {

    @kotlinx.serialization.Transient
    private var cachedPolygons: List<CSGPolygon>? = null

    public fun getPolygons(): List<CSGPolygon> {
        if (cachedPolygons == null) {
            cachedPolygons = polygonData.map { it.toCSGPolygon() }
        }
        return cachedPolygons!!
    }

    override fun <T : Any> toGeometry(geometryBuilder: GeometryBuilder<T>) {
        for (raw in getPolygons()) {
            for (polygon in triangulatePolygon(raw)) {
                val a = polygon.vertices[0]
                val b = polygon.vertices[1]
                val c = polygon.vertices[2]

                // Average per-vertex normals into one face normal.
                // For flat surfaces (box, cylinder) all three are identical.
                // For curved surfaces (sphere) they differ — averaging gives
                // smooth shading without per-vertex normal support in GeometryBuilder
                val norm = Float32Vector3D(
                    ((a.normal.x + b.normal.x + c.normal.x) / 3.0).toFloat(),
                    ((a.normal.y + b.normal.y + c.normal.y) / 3.0).toFloat(),
                    ((a.normal.z + b.normal.z + c.normal.z) / 3.0).toFloat()
                )

                geometryBuilder.face(
                    Float32Vector3D(a.position.x.toFloat(), a.position.y.toFloat(), a.position.z.toFloat()),
                    Float32Vector3D(b.position.x.toFloat(), b.position.y.toFloat(), b.position.z.toFloat()),
                    Float32Vector3D(c.position.x.toFloat(), c.position.y.toFloat(), c.position.z.toFloat()),
                    norm,
                    Meta.EMPTY
                )
            }
        }
    }

    public fun union(other: CSGSolid): CSGSolid {
        val result = csgUnion(getPolygons(), other.getPolygons()).flatMap(::triangulatePolygon)
        return CSGSolid(result.map { CSGPolygonData.fromCSGPolygon(it) })
    }

    public fun subtract(other: CSGSolid): CSGSolid {
        val result = csgSubtract(getPolygons(), other.getPolygons()).flatMap(::triangulatePolygon)
        return CSGSolid(result.map { CSGPolygonData.fromCSGPolygon(it) })
    }

    public fun intersect(other: CSGSolid): CSGSolid {
        val result = csgIntersect(getPolygons(), other.getPolygons()).flatMap(::triangulatePolygon)
        return CSGSolid(result.map { CSGPolygonData.fromCSGPolygon(it) })
    }

    public fun polygonCount(): Int = getPolygons().size

    public fun calculateVolume(): Double = calculateVolume(getPolygons())

    public companion object {
        public fun fromPolygons(polygons: List<CSGPolygon>): CSGSolid =
            CSGSolid(polygons.map { CSGPolygonData.fromCSGPolygon(it) })
    }
}

// Serialization DTO
@Serializable
public data class CSGVertexData(
    val px: Double, val py: Double, val pz: Double,
    val nx: Double, val ny: Double, val nz: Double
) {
    public fun toCSGVertex(): CSGVertex = CSGVertex(
        position = Float64Space3D.vector(px, py, pz),
        normal   = Float64Space3D.vector(nx, ny, nz)
    )

    public companion object {
        public fun fromCSGVertex(v: CSGVertex): CSGVertexData = CSGVertexData(
            px = v.position.x, py = v.position.y, pz = v.position.z,
            nx = v.normal.x,   ny = v.normal.y,   nz = v.normal.z
        )
    }
}

@Serializable
public data class CSGPolygonData(
    val vertices: List<CSGVertexData>,
    val shared: Int? = null
) {
    public fun toCSGPolygon(): CSGPolygon =
        CSGPolygon(vertices = vertices.map { it.toCSGVertex() }, shared = shared)

    public companion object {
        public fun fromCSGPolygon(p: CSGPolygon): CSGPolygonData =
            CSGPolygonData(vertices = p.vertices.map { CSGVertexData.fromCSGVertex(it) }, shared = p.shared)
    }
}

// CSGGeometryCollector — drives GeometrySolid.toGeometry() and collects
// the resulting triangles as CSGPolygon instances.
private class CSGGeometryCollector : GeometryBuilder<Unit> {
    val polygons = mutableListOf<CSGPolygon>()

    override fun face(
        vertex1: FloatVector3D,
        vertex2: FloatVector3D,
        vertex3: FloatVector3D,
        normal: FloatVector3D?,
        meta: Meta
    ) {
        val n = normal ?: calculateNormal(vertex1, vertex2, vertex3)
        polygons.add(CSGPolygon(listOf(
            vertex1.toCSGVertex(n),
            vertex2.toCSGVertex(n),
            vertex3.toCSGVertex(n)
        )))
    }

    override fun build() {}

    private fun calculateNormal(v1: FloatVector3D, v2: FloatVector3D, v3: FloatVector3D): FloatVector3D =
        with(Float32Space3D) {
            val cross = (v2 - v1) cross (v3 - v1)
            val len = norm(cross)
            if (len > 1e-5f) cross * (1.0f / len) else cross
        }

    private fun FloatVector3D.toCSGVertex(n: FloatVector3D): CSGVertex = CSGVertex(
        position = Float64Space3D.vector(x.toDouble(), y.toDouble(), z.toDouble()),
        normal   = Float64Space3D.vector(n.x.toDouble(), n.y.toDouble(), n.z.toDouble())
    )
}

// Sphere.toGeometry() calls face4() without normals, so CSGGeometryCollector
// would fall back to flat cross-product normals, giving faceted shading.
// This function produces radial per-vertex normals using the same spherical
// coordinate convention as Sphere.kt, without requiring changes to that file
private fun sphereVertex(center: Float64Vector3D, radius: Double, theta: Double, phi: Double): CSGVertex =
    with(Float64Space3D) {
        val x = -radius * sin(theta) * cos(phi)
        val y =  radius * cos(theta)
        val z =  radius * sin(theta) * sin(phi)
        val pos = center + vector(x, y, z)
        val len = kotlin.math.sqrt(x * x + y * y + z * z)
        val norm = if (len > 1e-5) vector(x / len, y / len, z / len) else vector(x, y, z)
        CSGVertex(pos, norm)
    }

private fun collectSpherePolygons(center: Float64Vector3D, radius: Double, segments: Int): List<CSGPolygon> {
    val phiStep   = 2.0 * PI / segments
    val thetaStep = PI / segments
    val polygons  = mutableListOf<CSGPolygon>()

    // Top cap
    for (j in 0 until segments) {
        polygons += CSGPolygon(listOf(
            sphereVertex(center, radius, 0.0,       0.0),
            sphereVertex(center, radius, thetaStep, j                    * phiStep),
            sphereVertex(center, radius, thetaStep, ((j + 1) % segments) * phiStep)
        ))
    }

    // Middle bands
    for (i in 1 until segments - 1) {
        val theta1 = i       * thetaStep
        val theta2 = (i + 1) * thetaStep
        for (j in 0 until segments) {
            val phi1 = j                    * phiStep
            val phi2 = ((j + 1) % segments) * phiStep
            val v00 = sphereVertex(center, radius, theta1, phi1)
            val v10 = sphereVertex(center, radius, theta2, phi1)
            val v11 = sphereVertex(center, radius, theta2, phi2)
            val v01 = sphereVertex(center, radius, theta1, phi2)
            polygons += CSGPolygon(listOf(v00, v10, v11))
            polygons += CSGPolygon(listOf(v00, v11, v01))
        }
    }

    // Bottom cap
    val lastTheta = PI - thetaStep
    for (j in 0 until segments) {
        polygons += CSGPolygon(listOf(
            sphereVertex(center, radius, PI,        0.0),
            sphereVertex(center, radius, lastTheta, ((j + 1) % segments) * phiStep),
            sphereVertex(center, radius, lastTheta, j                    * phiStep)
        ))
    }

    return polygons
}

// Transformations
private data class Matrix3x3(
    val m00: Double, val m01: Double, val m02: Double,
    val m10: Double, val m11: Double, val m12: Double,
    val m20: Double, val m21: Double, val m22: Double
) {
    fun transform(v: Float64Vector3D) = with(Float64Space3D) {
        vector(
            m00 * v.x + m01 * v.y + m02 * v.z,
            m10 * v.x + m11 * v.y + m12 * v.z,
            m20 * v.x + m21 * v.y + m22 * v.z
        )
    }
}

private fun buildRotationMatrix(rotX: Double, rotY: Double, rotZ: Double): Matrix3x3 {
    val cx = cos(rotX); val sx = sin(rotX)
    val cy = cos(rotY); val sy = sin(rotY)
    val cz = cos(rotZ); val sz = sin(rotZ)
    return Matrix3x3(
        m00 =  cy * cz,                m01 = -cy * sz,                m02 =  sy,
        m10 =  sx * sy * cz + cx * sz, m11 = -sx * sy * sz + cx * cz, m12 = -sx * cy,
        m20 = -cx * sy * cz + sx * sz, m21 =  cx * sy * sz + sx * cz, m22 =  cx * cy
    )
}

private fun applyTransformations(polygons: List<CSGPolygon>, solid: Solid): List<CSGPolygon> {
    val position = solid.position ?: Float32Space3D.zero
    val rotMatrix = buildRotationMatrix(
        (solid.rotationX).toDouble(),
        (solid.rotationY).toDouble(),
        (solid.rotationZ).toDouble()
    )
    val scaleX = (solid.scaleX).toDouble()
    val scaleY = (solid.scaleY).toDouble()
    val scaleZ = (solid.scaleZ).toDouble()

    return polygons.map { polygon ->
        CSGPolygon(
            vertices = polygon.vertices.map { vertex ->
                with(Float64Space3D) {
                    val scaled     = vector(vertex.position.x * scaleX, vertex.position.y * scaleY, vertex.position.z * scaleZ)
                    val rotated    = rotMatrix.transform(scaled)
                    val translated = rotated + vector(position.x.toDouble(), position.y.toDouble(), position.z.toDouble())
                    CSGVertex(translated, rotMatrix.transform(vertex.normal))
                }
            },
            shared = polygon.shared
        )
    }
}

// Convert GeometrySolid primitives to a CSGSolid ready for boolean operations
// All primitives go through their .toGeometry implementation
public fun GeometrySolid.toCSGSolid(): CSGSolid {
    val polygons = when (this) {
        is Sphere -> collectSpherePolygons(
            center   = Float64Space3D.zero,
            radius   = this.radius.toDouble(),
            segments = this.detail ?: 32
        )
        else -> {
            val collector = CSGGeometryCollector()
            toGeometry(collector)
            collector.polygons
        }
    }

    return CSGSolid.fromPolygons(applyTransformations(polygons, this as Solid))
}

public operator fun CSGSolid.plus(other: CSGSolid): CSGSolid  = this.union(other)
public operator fun CSGSolid.minus(other: CSGSolid): CSGSolid = this.subtract(other)