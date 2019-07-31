package hep.dataforge.vis.spatial

import hep.dataforge.vis.common.VisualObject

class Convex(parent: VisualObject?, val points: List<Point3D>) : VisualLeaf3D(parent) {


    companion object {
        const val TYPE = "geometry.3d.convex"
    }
}

fun VisualGroup3D.convex(action: ConvexBuilder.() -> Unit = {}) =
    ConvexBuilder().apply(action).build(this).also { add(it) }

class ConvexBuilder {
    private val points = ArrayList<Point3D>()

    fun point(x: Number, y: Number, z: Number) {
        points.add(Point3D(x, y, z))
    }

    fun build(parent: VisualObject?): Convex {
        return Convex(parent, points)
    }
}