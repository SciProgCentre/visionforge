package hep.dataforge.vis.spatial

import hep.dataforge.meta.EmptyMeta
import hep.dataforge.meta.Meta
import hep.dataforge.vis.DisplayGroup
import hep.dataforge.vis.DisplayLeaf
import hep.dataforge.vis.DisplayObject
import hep.dataforge.vis.double

class Box(parent: DisplayObject?, meta: Meta) : DisplayLeaf(parent, TYPE, meta) {
    var xSize by double(1.0)
    var ySize by double(1.0)
    var zSize by double(1.0)

    //TODO add helper for color configuration

    companion object {
        const val TYPE = "geometry.spatial.box"
    }
}

fun DisplayGroup.box(meta: Meta = EmptyMeta, action: Box.() -> Unit = {}) =
    Box(this, meta).apply(action).also { addChild(it) }