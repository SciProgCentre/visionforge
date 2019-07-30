package hep.dataforge.vis.spatial

import hep.dataforge.meta.Meta
import hep.dataforge.vis.common.VisualGroup
import hep.dataforge.vis.common.VisualObject

class Convex(parent: VisualObject?, val points: List<Point3D>, meta: Array<out Meta>) : VisualLeaf3D(parent, meta) {


    companion object {
        const val TYPE = "geometry.3d.convex"
    }
}

fun VisualGroup.convex(vararg meta: Meta, action: ConvexBuilder.() -> Unit = {}) =
    ConvexBuilder().apply(action).build(this, meta).also { add(it) }

class ConvexBuilder {
    private val points = ArrayList<Point3D>()

    fun point(x: Number, y: Number, z: Number) {
        points.add(Point3D(x, y, z))
    }

    fun build(parent: VisualObject?, meta: Array<out Meta>): Convex {
        return Convex(parent, points, meta)
    }
}