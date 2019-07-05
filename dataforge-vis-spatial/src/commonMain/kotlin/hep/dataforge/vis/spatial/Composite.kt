package hep.dataforge.vis.spatial

import hep.dataforge.meta.EmptyMeta
import hep.dataforge.meta.Meta
import hep.dataforge.meta.seal
import hep.dataforge.vis.common.DisplayGroup
import hep.dataforge.vis.common.DisplayLeaf
import hep.dataforge.vis.common.DisplayObject

enum class CompositeType {
    UNION,
    INTERSECT,
    SUBTRACT
}

class Composite(
    parent: DisplayObject?,
    val first: DisplayObject,
    val second: DisplayObject,
    val type: CompositeType = CompositeType.UNION,
    meta: Meta = EmptyMeta
) : DisplayLeaf(parent, meta)

fun DisplayGroup.composite(type: CompositeType, builder: DisplayGroup.() -> Unit): Composite {
    val group = DisplayGroup().apply(builder)
    val children = group.toList()
    if (children.size != 2) error("Composite requires exactly two children")
    return Composite(this, children[0], children[1], type, group.properties.seal()).also { add(it) }
}

fun DisplayGroup.union(builder: DisplayGroup.() -> Unit) =
    composite(CompositeType.UNION,builder)

fun DisplayGroup.subtract(builder: DisplayGroup.() -> Unit) =
    composite(CompositeType.SUBTRACT,builder)

fun DisplayGroup.intersect(builder: DisplayGroup.() -> Unit) =
    composite(CompositeType.INTERSECT,builder)