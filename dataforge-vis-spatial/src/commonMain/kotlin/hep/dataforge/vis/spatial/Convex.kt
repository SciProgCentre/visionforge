package hep.dataforge.vis.spatial

import hep.dataforge.meta.*
import hep.dataforge.vis.common.VisualGroup
import hep.dataforge.vis.common.DisplayLeaf
import hep.dataforge.vis.common.VisualObject

class Convex(parent: VisualObject?, meta: Meta) : DisplayLeaf(parent, meta) {

    val points = points(properties["points"] ?: error("Vertices not defined"))

    companion object {
        const val TYPE = "geometry.3d.convex"

        fun points(item: MetaItem<*>): List<Point3D> {
            return item.node?.getAll("point")?.map { (_, value) ->
                Point3D.from(value.node?: error("Point definition is not a node"))
            } ?: emptyList()
        }
    }
}

fun VisualGroup.convex(meta: Meta = EmptyMeta, action: ConvexBuilder.() -> Unit = {}) =
    ConvexBuilder().apply(action).build(this, meta).also { add(it) }

class ConvexBuilder {
    private val points = ArrayList<Point3D>()

    fun point(x: Number, y: Number, z: Number) {
        points.add(Point3D(x, y, z))
    }

    fun build(parent: VisualObject?, meta: Meta): Convex {
        val points = buildMeta {
            points.forEachIndexed { index, value ->
                "points.point[$index]" to value.toMeta()
            }
        }.seal()

        return Convex(parent, points)
    }
}