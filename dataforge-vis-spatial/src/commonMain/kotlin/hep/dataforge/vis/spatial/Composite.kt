package hep.dataforge.vis.spatial

import hep.dataforge.meta.EmptyMeta
import hep.dataforge.meta.Meta
import hep.dataforge.vis.common.DisplayLeaf
import hep.dataforge.vis.common.DisplayObject

class Composite(
    parent: DisplayObject?,
    val first: DisplayObject,
    val second: DisplayObject,
    meta: Meta = EmptyMeta
) : DisplayLeaf(parent,meta)