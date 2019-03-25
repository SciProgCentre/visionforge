package hep.dataforge.vis.spatial

import hep.dataforge.meta.*
import hep.dataforge.names.toName
import hep.dataforge.vis.DisplayGroup
import hep.dataforge.vis.DisplayLeaf
import hep.dataforge.vis.DisplayObject

class Convex(parent: DisplayObject?, meta: Meta) : DisplayLeaf(parent, TYPE, meta) {

    val points = points(properties["points"] ?: error("Vertices not defined"))

    companion object {
        const val TYPE = "geometry.3d.convex"

        fun points(item: MetaItem<*>): List<Point3D> {
            return item.node?.getAll("point".toName())?.map { (_, value) ->
                Point3D(value.node["x"].number ?: 0, value.node["y"].number ?: 0, value.node["y"].number ?: 0)
            } ?: emptyList()
        }
    }
}

fun DisplayGroup.convex(meta: Meta = EmptyMeta, action: ConvexBuilder.() -> Unit = {}) =
    ConvexBuilder().apply(action).build(this, meta).also { addChild(it) }

class ConvexBuilder {
    private val points = ArrayList<Point3D>()

    fun point(x: Number, y: Number, z: Number) {
        points.add(Point3D(x, y, z))
    }

    fun build(parent: DisplayObject?, meta: Meta): Convex {
        val builder = meta.builder()
        points.forEachIndexed { index, value ->
            builder["points.point[$index]"] = value.toMeta()
        }
        return Convex(parent, builder.seal())
    }
}