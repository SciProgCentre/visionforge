package space.kscience.visionforge.solid

import space.kscience.kmath.geometry.Vector3D
import space.kscience.kmath.geometry.euclidean3d.Float64Space3D
import space.kscience.kmath.structures.Float64
import kotlin.math.abs

// Epsilon for floating-point comparisons
private const val EPSILON = 1e-5

public enum class PolygonType {
    COPLANAR,
    FRONT,
    BACK,
    SPANNING
}

// vertex with position and normal
public data class CSGVertex(
    val position: Vector3D<Float64>,
    val normal: Vector3D<Float64>
) {
    // linearly interpolation between this vertex and another
    public fun interpolate(other: CSGVertex, t: Double): CSGVertex = with(Float64Space3D) {
        val newPosition = position + (other.position - position) * t
        val newNormal = (normal + (other.normal - normal) * t).let {
            val len = norm(it)
            if (len > EPSILON) it * (1.0 / len) else it
        }
        CSGVertex(newPosition, newNormal)
    }

    public fun flipped(): CSGVertex = with(Float64Space3D) {
        copy(normal = -normal)
    }
}

public data class CSGPolygon(
    val vertices: List<CSGVertex>,
    val shared: Int? = null
) {
    init {
        require(vertices.size >= 3) { "Polygon must have at least 3 vertices" }
    }

    // plane equation for this polygon
    public val plane: CSGPlane by lazy {
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

    // flip the polygon (reverse vertex order and normals)
    public fun flipped(): CSGPolygon = CSGPolygon(
        vertices.asReversed().map { it.flipped() },
        shared
    )
}

// plane in 3D space defined by (normal * w)
public data class CSGPlane(
    val normal: Vector3D<Float64>,
    val w: Double
) {
    // classify point relative to this plane
    public fun classifyPoint(point: Vector3D<Float64>): PolygonType = with(Float64Space3D) {
        val t = normal.dot(point) - w
        return when {
            t < -EPSILON -> PolygonType.BACK
            t > EPSILON -> PolygonType.FRONT
            else -> PolygonType.COPLANAR
        }
    }

    // split polygon by this plane
    public fun splitPolygon(
        polygon: CSGPolygon,
        coplanarFront: MutableList<CSGPolygon>,
        coplanarBack: MutableList<CSGPolygon>,
        front: MutableList<CSGPolygon>,
        back: MutableList<CSGPolygon>
    ) {
        // classify all vertices
        val types = polygon.vertices.map { classifyPoint(it.position) }

        // determine polygon type
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
                // check normal direction to decide front or back
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
                        // calculate interpolation parameter
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

    public fun flipped(): CSGPlane = with(Float64Space3D) {
        CSGPlane(-normal, -w)
    }
}

// node in BSP tree
public class BSPNode(polygons: List<CSGPolygon>? = null) {
    public var plane: CSGPlane? = null
    public var front: BSPNode? = null
    public var back: BSPNode? = null
    public val polygons: MutableList<CSGPolygon> = mutableListOf()

    init {
        if (polygons != null) build(polygons)
    }

    // build BSP tree from a list of polygons
    public fun build(polygons: List<CSGPolygon>) {
        if (polygons.isEmpty()) return

        // choose first splitting plane
        if (plane == null) {
            plane = polygons[0].plane
        }

        val frontList = mutableListOf<CSGPolygon>()
        val backList = mutableListOf<CSGPolygon>()

        for (polygon in polygons) {
            plane!!.splitPolygon(
                polygon,
                this.polygons,
                this.polygons,
                frontList,
                backList
            )
        }

        // recursively build subtrees
        if (frontList.isNotEmpty()) {
            if (front == null) front = BSPNode()
            front!!.build(frontList)
        }
        if (backList.isNotEmpty()) {
            if (back == null) back = BSPNode()
            back!!.build(backList)
        }
    }

    // invert this BSP tree
    public fun invert() {
        // Flip all polygons
        for (i in polygons.indices) {
            polygons[i] = polygons[i].flipped()
        }

        // flip the plane
        plane = plane?.flipped()

        // invert subtrees and swap them
        front?.invert()
        back?.invert()
        val temp = front
        front = back
        back = temp
    }

    // clip list of polygons against this BSP tree
    public fun clipPolygons(polygons: List<CSGPolygon>): List<CSGPolygon> {
        if (plane == null) return polygons.toList()

        var frontList = mutableListOf<CSGPolygon>()
        var backList = mutableListOf<CSGPolygon>()

        for (polygon in polygons) {
            plane!!.splitPolygon(polygon, frontList, backList, frontList, backList)
        }

        if (front != null) {
            frontList = front!!.clipPolygons(frontList).toMutableList()
        }

        backList = if (back != null) {
            back!!.clipPolygons(backList).toMutableList()
        } else {
            mutableListOf()
        }

        return frontList + backList
    }

    // clip this BSP tree against another BSP tree
    public fun clipTo(bsp: BSPNode) {
        val clipped = bsp.clipPolygons(polygons)
        polygons.clear()
        polygons.addAll(clipped)
        front?.clipTo(bsp)
        back?.clipTo(bsp)
    }

    // return all polygons in this BSP tree
    public fun allPolygons(): List<CSGPolygon> {
        var result = polygons.toMutableList()
        front?.let { result.addAll(it.allPolygons()) }
        back?.let { result.addAll(it.allPolygons()) }
        return result
    }
}

// filter invalid polygons
// based on three-csg approach: filter(p => !Number.isNaN(p.plane.normal.x))
private fun filterValidPolygons(polygons: List<CSGPolygon>): List<CSGPolygon> {
    return polygons.filter { polygon ->
        // Check vertex count
        if (polygon.vertices.size < 3) return@filter false

        // Check for valid normals (no NaN, no Infinite)
        val n = polygon.plane.normal
        if (!n.x.isFinite() || !n.y.isFinite() || !n.z.isFinite()) return@filter false

        // Check for degenerate polygon (zero area)
        // Calculate area using cross product of first two edges
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

public fun csgUnion(a: List<CSGPolygon>, b: List<CSGPolygon>): List<CSGPolygon> {
    val nodeA = BSPNode(a)
    val nodeB = BSPNode(b)

    nodeA.clipTo(nodeB)
    nodeB.clipTo(nodeA)
    nodeB.invert()
    nodeB.clipTo(nodeA)
    nodeB.invert()
    nodeA.build(nodeB.allPolygons())

    return filterValidPolygons(nodeA.allPolygons())
}

public fun csgSubtract(a: List<CSGPolygon>, b: List<CSGPolygon>): List<CSGPolygon> {
    val nodeA = BSPNode(a)
    val nodeB = BSPNode(b)

    nodeA.invert()
    nodeA.clipTo(nodeB)
    nodeB.clipTo(nodeA)
    nodeB.invert()
    nodeB.clipTo(nodeA)
    nodeB.invert()
    nodeA.build(nodeB.allPolygons())
    nodeA.invert()

    return filterValidPolygons(nodeA.allPolygons())
}

public fun csgIntersect(a: List<CSGPolygon>, b: List<CSGPolygon>): List<CSGPolygon> {
    val nodeA = BSPNode(a)
    val nodeB = BSPNode(b)

    nodeA.invert()
    nodeB.clipTo(nodeA)
    nodeB.invert()
    nodeA.clipTo(nodeB)
    nodeB.clipTo(nodeA)
    nodeA.build(nodeB.allPolygons())
    nodeA.invert()

    return filterValidPolygons(nodeA.allPolygons())
}

// for tests
public fun calculateVolume(polygons: List<CSGPolygon>): Double {
    var volume = 0.0

    for (polygon in polygons) {
        if (polygon.vertices.size < 3) continue

        val v0 = polygon.vertices[0].position
        for (i in 1 until polygon.vertices.size - 1) {
            val v1 = polygon.vertices[i].position
            val v2 = polygon.vertices[i + 1].position

            with(Float64Space3D) {
                volume += v0.dot(v1 cross v2) / 6.0
            }
        }
    }

    return abs(volume)
}