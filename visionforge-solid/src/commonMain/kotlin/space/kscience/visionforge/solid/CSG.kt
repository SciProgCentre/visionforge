package space.kscience.visionforge.solid

import space.kscience.kmath.geometry.Vector3D
import space.kscience.kmath.geometry.euclidean3d.Float64Space3D
import space.kscience.kmath.structures.Float64
import kotlin.math.abs

import space.kscience.dataforge.meta.Meta
import space.kscience.kmath.geometry.euclidean3d.Float32Space3D
import space.kscience.kmath.geometry.euclidean3d.Float32Vector3D
import space.kscience.kmath.geometry.euclidean3d.Float64Vector3D
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private const val EPSILON = 1e-5

/**
 * Classification of a polygon relative to a plane.
 */
public enum class PolygonType {
    COPLANAR, // Polygon lies on the plane
    FRONT, // Polygon is in front of the plane
    BACK, // Polygon is behind the plane
    SPANNING // Polygon spans across the plane
}

/**
 * 3D vertex with position and normal.
 */
public data class CSGVertex(
    val position: Vector3D<Float64>, // 3D position of the vertex
    val normal: Vector3D<Float64> // Normal vector at the vertex
) {
    /**
     * Interpolates between this vertex and [other] vertex at parameter [t].
     *
     * @param other Target vertex for interpolation
     * @param t Interpolation parameter in range [0.0, 1.0]
     */
    public fun interpolate(other: CSGVertex, t: Double): CSGVertex = with(Float64Space3D) {
        val newPosition = position + (other.position - position) * t
        val newNormal = (normal + (other.normal - normal) * t).let {
            val len = norm(it)
            if (len > EPSILON) it * (1.0 / len) else it
        }
        CSGVertex(newPosition, newNormal)
    }

    /**
     * Returns a vertex with flipped normal direction.
     */
    public fun flipped(): CSGVertex = with(Float64Space3D) {
        copy(normal = -normal)
    }
}

/**
 * Convex polygon with vertices in CCW order.
 */
public data class CSGPolygon(
    val vertices: List<CSGVertex>, // List of vertices in counter-clockwise order
    val shared: Int? = null // Optional shared material/property identifier
) {
    init {
        require(vertices.size >= 3) { "Polygon must have at least 3 vertices" }
    }

    /** The plane equation computed from the first three vertices */
    val plane: CSGPlane by lazy {
        with(Float64Space3D) {
            val a = vertices[0].position
            val b = vertices[1].position
            val c = vertices[2].position
            val normal = ((b - a) cross (c - a)).let {
                val len = norm(it)
                if (len > EPSILON) it * (1.0 / len) else it
            }
            val w = normal.dot(a)
            CSGPlane(normal, w)
        }
    }

    /**
     * Returns a polygon with flipped orientation and reversed vertex order.
     */
    public fun flipped(): CSGPolygon = CSGPolygon(
        vertices.asReversed().map { it.flipped() },
        shared
    )
}

/**
 * Plane equation: normal · point = w.
 */
public data class CSGPlane(
    val normal: Vector3D<Float64>, // Unit normal vector of the plane
    val w: Double // Distance from origin along the normal
) {
    /**
     * Classifies a [point] relative to this plane.
     *
     * @param point The point to classify
     */
    public fun classifyPoint(point: Vector3D<Float64>): PolygonType = with(Float64Space3D) {
        val t = normal.dot(point) - w
        return when {
            t < -EPSILON -> PolygonType.BACK
            t > EPSILON -> PolygonType.FRONT
            else -> PolygonType.COPLANAR
        }
    }

    /**
     * Splits [polygon] by this plane into coplanar, front, and back lists.
     *
     * @param polygon Polygon to split
     * @param coplanarFront Output list for coplanar polygons facing front
     * @param coplanarBack Output list for coplanar polygons facing back
     * @param front Output list for front-side polygons
     * @param back Output list for back-side polygons
     */
    public fun splitPolygon(
        polygon: CSGPolygon,
        coplanarFront: MutableList<CSGPolygon>,
        coplanarBack: MutableList<CSGPolygon>,
        front: MutableList<CSGPolygon>,
        back: MutableList<CSGPolygon>
    ) {
        val types = polygon.vertices.map { classifyPoint(it.position) }

        val hasFront = types.any { it == PolygonType.FRONT }
        val hasBack = types.any { it == PolygonType.BACK }

        val polygonType = when {
            hasFront && hasBack -> PolygonType.SPANNING
            hasFront -> PolygonType.FRONT
            hasBack -> PolygonType.BACK
            else -> PolygonType.COPLANAR
        }

        when (polygonType) {
            PolygonType.COPLANAR -> {
                val sameDirection = with(Float64Space3D) { normal.dot(polygon.plane.normal) > 0 }
                if (sameDirection) coplanarFront.add(polygon) else coplanarBack.add(polygon)
            }
            PolygonType.FRONT -> front.add(polygon)
            PolygonType.BACK -> back.add(polygon)
            PolygonType.SPANNING -> {
                val f = mutableListOf<CSGVertex>()
                val b = mutableListOf<CSGVertex>()

                for (i in polygon.vertices.indices) {
                    val j = (i + 1) % polygon.vertices.size
                    val ti = types[i]
                    val tj = types[j]
                    val vi = polygon.vertices[i]
                    val vj = polygon.vertices[j]

                    if (ti != PolygonType.BACK) f.add(vi)
                    if (ti != PolygonType.FRONT) b.add(vi)

                    if ((ti == PolygonType.FRONT && tj == PolygonType.BACK) ||
                        (ti == PolygonType.BACK && tj == PolygonType.FRONT)
                    ) {
                        val t = with(Float64Space3D) {
                            val denominator = normal.dot(vj.position - vi.position)
                            if (abs(denominator) < EPSILON) 0.0
                            else (w - normal.dot(vi.position)) / denominator
                        }
                        val v = vi.interpolate(vj, t)
                        f.add(v)
                        b.add(v)
                    }
                }

                if (f.size >= 3) front.add(CSGPolygon(f, polygon.shared))
                if (b.size >= 3) back.add(CSGPolygon(b, polygon.shared))
            }
        }
    }

    /**
     * Returns a plane with flipped normal direction.
     */
    public fun flipped(): CSGPlane = with(Float64Space3D) {
        CSGPlane(-normal, -w)
    }
}

/**
 * BSP tree node for spatial partitioning.
 */
private data class BSPNode(
    val plane: CSGPlane? = null,
    val front: BSPNode? = null,
    val back: BSPNode? = null,
    val polygons: List<CSGPolygon> = emptyList()
) {
    /**
     * Builds tree by recursively partitioning polygons.
     */
    fun build(newPolygons: List<CSGPolygon>): BSPNode {
        if (newPolygons.isEmpty()) return this

        val currentPlane = plane ?: newPolygons[0].plane
        val coplanar = mutableListOf<CSGPolygon>()
        val frontList = mutableListOf<CSGPolygon>()
        val backList  = mutableListOf<CSGPolygon>()

        for (polygon in newPolygons) {
            currentPlane.splitPolygon(polygon, coplanar, coplanar, frontList, backList)
        }

        return BSPNode(
            plane    = currentPlane,
            front    = if (frontList.isNotEmpty()) (front ?: BSPNode()).build(frontList) else front,
            back     = if (backList.isNotEmpty())  (back  ?: BSPNode()).build(backList)  else back,
            polygons = polygons + coplanar
        )
    }

    /**
     * Inverts the tree by flipping all planes and polygons.
     */
    fun invert(): BSPNode = BSPNode(
        plane    = plane?.flipped(),
        front    = back?.invert(),
        back     = front?.invert(),
        polygons = polygons.map { it.flipped() }
    )

    /**
     * Clips polygons against this BSP tree, keeping only parts inside.
     */
    fun clipPolygons(polygons: List<CSGPolygon>): List<CSGPolygon> {
        if (plane == null) return polygons

        val frontList = mutableListOf<CSGPolygon>()
        val backList  = mutableListOf<CSGPolygon>()

        for (polygon in polygons) {
            plane.splitPolygon(polygon, frontList, backList, frontList, backList)
        }

        return (front?.clipPolygons(frontList) ?: frontList) +
                (back?.clipPolygons(backList)   ?: emptyList())
    }

    /**
     * Clips this tree to another BSP tree.
     */
    fun clipTo(bsp: BSPNode): BSPNode = BSPNode(
        plane    = plane,
        front    = front?.clipTo(bsp),
        back     = back?.clipTo(bsp),
        polygons = bsp.clipPolygons(polygons)
    )

    /**
     * Returns all polygons in this tree.
     */
    fun allPolygons(): List<CSGPolygon> =
        polygons + (front?.allPolygons() ?: emptyList()) + (back?.allPolygons() ?: emptyList())
}

/**
 * Filters out degenerate or invalid polygons.
 */
private fun filterValidPolygons(polygons: List<CSGPolygon>): List<CSGPolygon> {
    return polygons.filter { polygon ->
        if (polygon.vertices.size < 3) return@filter false

        val n = polygon.plane.normal
        if (!n.x.isFinite() || !n.y.isFinite() || !n.z.isFinite()) return@filter false

        with(Float64Space3D) {
            val v0 = polygon.vertices[0].position
            val v1 = polygon.vertices[1].position
            val v2 = polygon.vertices[2].position

            val edge1 = v1 - v0
            val edge2 = v2 - v0
            val cross = edge1 cross edge2
            val area = norm(cross)

            if (area < EPSILON) return@filter false
        }

        true
    }
}

/**
 * Performs CSG union operation on two sets of polygons.
 */
private fun csgUnion(a: List<CSGPolygon>, b: List<CSGPolygon>): List<CSGPolygon> {
    val nodeA = BSPNode().build(a)
    val nodeB = BSPNode().build(b)
    val clippedA = nodeA.clipTo(nodeB)
    val clippedB = nodeB.clipTo(clippedA).invert().clipTo(clippedA).invert()
    return filterValidPolygons(clippedA.build(clippedB.allPolygons()).allPolygons())
}

/**
 * Performs CSG subtraction operation on two sets of polygons.
 */
private fun csgSubtract(a: List<CSGPolygon>, b: List<CSGPolygon>): List<CSGPolygon> {
    val nodeA = BSPNode().build(a).invert()
    val nodeB = BSPNode().build(b)
    val clippedA = nodeA.clipTo(nodeB)
    val clippedB = nodeB.clipTo(clippedA).invert().clipTo(clippedA).invert()
    return filterValidPolygons(clippedA.build(clippedB.allPolygons()).invert().allPolygons())
}

/**
 * Performs CSG intersection operation on two sets of polygons.
 */
private fun csgIntersect(a: List<CSGPolygon>, b: List<CSGPolygon>): List<CSGPolygon> {
    val nodeA = BSPNode().build(a).invert()
    val nodeB = BSPNode().build(b).clipTo(BSPNode().build(a).invert())
    val clippedB = nodeB.invert()
    val clippedA = nodeA.clipTo(clippedB)
    return filterValidPolygons(clippedA.build(clippedB.clipTo(clippedA).allPolygons()).invert().allPolygons())
}

/**
 * Triangulates a polygon by fan triangulation.
 */
private fun triangulatePolygon(p: CSGPolygon): List<CSGPolygon> {
    if (p.vertices.size == 3) return listOf(p)
    val out = mutableListOf<CSGPolygon>()
    val v0 = p.vertices[0]
    for (i in 1 until p.vertices.size - 1) {
        out += CSGPolygon(listOf(v0, p.vertices[i], p.vertices[i + 1]), p.shared)
    }
    return out
}

/**
 * Serialization data structure for CSG vertex.
 */
private data class CSGVertexData(
    val px: Double, val py: Double, val pz: Double,
    val nx: Double, val ny: Double, val nz: Double
) {
    /**
     * Converts this data structure to CSGVertex.
     */
    fun toCSGVertex(): CSGVertex = CSGVertex(
        position = Float64Space3D.vector(px, py, pz),
        normal   = Float64Space3D.vector(nx, ny, nz)
    )

    companion object {
        /**
         * Creates CSGVertexData from CSGVertex.
         */
        fun fromCSGVertex(v: CSGVertex): CSGVertexData = CSGVertexData(
            px = v.position.x, py = v.position.y, pz = v.position.z,
            nx = v.normal.x,   ny = v.normal.y,   nz = v.normal.z
        )
    }
}

/**
 * Serialization data structure for CSG polygon.
 */
private data class CSGPolygonData(
    val vertices: List<CSGVertexData>,
    val shared: Int? = null
) {
    /**
     * Converts this data structure to CSGPolygon.
     */
    fun toCSGPolygon(): CSGPolygon =
        CSGPolygon(vertices = vertices.map { it.toCSGVertex() }, shared = shared)

    companion object {
        /**
         * Creates CSGPolygonData from CSGPolygon.
         */
        fun fromCSGPolygon(p: CSGPolygon): CSGPolygonData =
            CSGPolygonData(vertices = p.vertices.map { CSGVertexData.fromCSGVertex(it) }, shared = p.shared)
    }
}

/**
 * Geometry builder that collects polygons for CSG operations.
 */
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

    /**
     * Calculates face normal from three vertices.
     */
    private fun calculateNormal(v1: FloatVector3D, v2: FloatVector3D, v3: FloatVector3D): FloatVector3D =
        with(Float32Space3D) {
            val cross = (v2 - v1) cross (v3 - v1)
            val len = norm(cross)
            if (len > 1e-5f) cross * (1.0f / len) else cross
        }

    /**
     * Converts Float32Vector3D to CSGVertex.
     */
    private fun FloatVector3D.toCSGVertex(n: FloatVector3D): CSGVertex = CSGVertex(
        position = Float64Space3D.vector(x.toDouble(), y.toDouble(), z.toDouble()),
        normal   = Float64Space3D.vector(n.x.toDouble(), n.y.toDouble(), n.z.toDouble())
    )
}

/**
 * Creates a sphere vertex at given spherical coordinates.
 */
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

/**
 * Generates sphere polygons using spherical tessellation.
 */
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

/**
 * 3x3 transformation matrix for rotation operations.
 */
private data class Matrix3x3(
    val m00: Double, val m01: Double, val m02: Double,
    val m10: Double, val m11: Double, val m12: Double,
    val m20: Double, val m21: Double, val m22: Double
) {
    /**
     * Transforms a vector by this matrix.
     */
    fun transform(v: Float64Vector3D) = with(Float64Space3D) {
        vector(
            m00 * v.x + m01 * v.y + m02 * v.z,
            m10 * v.x + m11 * v.y + m12 * v.z,
            m20 * v.x + m21 * v.y + m22 * v.z
        )
    }
}

/**
 * Builds a 3D rotation matrix from Euler angles.
 */
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

/**
 * Applies position, rotation, and scale transformations to polygons.
 */
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

/**
 * Converts a [GeometrySolid] to CSG polygon representation with applied transformations.
 */
public fun GeometrySolid.toCSGPolygons(): List<CSGPolygon> {
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

    return applyTransformations(polygons, this as Solid)
}

/**
 * builds CSG polygons into a geometry builder.
 */
private fun buildPolygons(polygons: List<CSGPolygon>, geometryBuilder: GeometryBuilder<*>) {
    for (raw in polygons) {
        for (polygon in triangulatePolygon(raw)) {
            val a = polygon.vertices[0]
            val b = polygon.vertices[1]
            val c = polygon.vertices[2]

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

/**
 * builds a [Composite] solid into [geometryBuilder] using CSG Boolean operations.
 *
 * Both [Composite.first] and [Composite.second] must implement [GeometrySolid].
 * The operation performed depends on [Composite.compositeType]:
 * - [CompositeType.UNION]: Combines both solids
 * - [CompositeType.SUBTRACT]: Removes second solid from first
 * - [CompositeType.INTERSECT]: Keeps only overlapping regions
 * - [CompositeType.GROUP]: Simple concatenation without Boolean operation
 *
 * @param composite The composite solid to render
 * @param geometryBuilder Target builder for output geometry
 * @throws IllegalArgumentException if either solid is not a [GeometrySolid]
 */
public fun buildComposite(
    composite: Composite,
    geometryBuilder: GeometryBuilder<*>
) {
    require(composite.first is GeometrySolid) { "First solid must be GeometrySolid" }
    require(composite.second is GeometrySolid) { "Second solid must be GeometrySolid" }

    val polygonsA = (composite.first).toCSGPolygons()
    val polygonsB = (composite.second).toCSGPolygons()

    val result = when (composite.compositeType) {
        CompositeType.UNION -> csgUnion(polygonsA, polygonsB)
        CompositeType.SUBTRACT -> csgSubtract(polygonsA, polygonsB)
        CompositeType.INTERSECT -> csgIntersect(polygonsA, polygonsB)
        CompositeType.GROUP -> polygonsA + polygonsB
    }

    buildPolygons(result, geometryBuilder)
}