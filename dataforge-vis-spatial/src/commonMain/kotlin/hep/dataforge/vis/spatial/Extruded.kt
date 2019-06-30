package hep.dataforge.vis.spatial

import hep.dataforge.meta.*
import hep.dataforge.names.toName
import hep.dataforge.vis.common.DisplayLeaf
import hep.dataforge.vis.common.DisplayObject
import hep.dataforge.vis.common.DisplayObjectList


typealias Shape2D = List<Point2D>

data class Layer(val z: Number, val x: Number = 0.0, val y: Number = 0.0, val scale: Number = 1.0)

class Extruded(parent: DisplayObject?, meta: Meta) : DisplayLeaf(parent, meta) {

    val shape
        get() = shape(properties["shape"] ?: error("Shape not defined"))

    val layers
        get() = properties.getAll("layer").values.map {
            layer(it.node ?: error("layer item is not a node"))
        }

    companion object {
        const val TYPE = "geometry.3d.extruded"

        private fun shape(item: MetaItem<*>): Shape2D {
            return item.node?.getAll("xyPoint".toName())?.map { (_, value) ->
                Point2D(value.node["x"].number ?: 0, value.node["y"].number ?: 0)
            } ?: emptyList()
        }

        private fun layer(meta: Meta): Layer {
            val x by meta.number(0.0)
            val y by meta.number(0.0)
            val z by meta.number { error("z is undefined in layer") }
            val scale by meta.number(1.0)
            return Layer(z, x, y, scale)
        }
    }
}


fun DisplayObjectList.extrude(meta: Meta = EmptyMeta, action: Extruded.() -> Unit = {}) =
    Extruded(this, meta).apply(action).also { addChild(it) }