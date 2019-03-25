package hep.dataforge.vis.spatial

import hep.dataforge.meta.*
import hep.dataforge.names.toName
import hep.dataforge.vis.DisplayGroup
import hep.dataforge.vis.DisplayLeaf
import hep.dataforge.vis.DisplayObject

class Extruded(parent: DisplayObject?, meta: Meta) : DisplayLeaf(parent, TYPE, meta) {

    val shape get() = shape(properties["shape"] ?: error("Shape not defined"))

    companion object {
        const val TYPE = "geometry.3d.extruded"

        fun shape(item: MetaItem<*>): Shape2D {
            return item.node?.getAll("xyPoint".toName())?.map { (_, value) ->
                Point2D(value.node["x"].number ?: 0, value.node["y"].number ?: 0)
            } ?: emptyList()
        }
    }
}


fun DisplayGroup.extrude(meta: Meta = EmptyMeta, action: Extruded.() -> Unit = {}) =
    Extruded(this, meta).apply(action).also { addChild(it) }