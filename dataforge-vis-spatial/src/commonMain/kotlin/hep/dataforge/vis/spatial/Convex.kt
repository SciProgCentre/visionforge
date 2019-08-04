package hep.dataforge.vis.spatial

import hep.dataforge.meta.MetaBuilder

class Convex(
    val points: List<Point3D>
) : VisualLeaf3D() {

    override fun MetaBuilder.updateMeta() {
        "points" to {
            "point" to points.map{it.toMeta()}
        }
    }

    companion object {
        const val TYPE = "geometry.3d.convex"
    }
}

inline fun VisualGroup3D.convex(name: String? = null, action: ConvexBuilder.() -> Unit = {}) =
    ConvexBuilder().apply(action).build().also { set(name, it) }

class ConvexBuilder {
    private val points = ArrayList<Point3D>()

    fun point(x: Number, y: Number, z: Number) {
        points.add(Point3D(x, y, z))
    }

    fun build(): Convex {
        return Convex(points)
    }
}