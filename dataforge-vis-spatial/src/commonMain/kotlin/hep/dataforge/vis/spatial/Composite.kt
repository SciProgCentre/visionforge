package hep.dataforge.vis.spatial

import hep.dataforge.meta.EmptyMeta
import hep.dataforge.meta.Meta
import hep.dataforge.meta.seal
import hep.dataforge.vis.common.DisplayLeaf
import hep.dataforge.vis.common.VisualGroup
import hep.dataforge.vis.common.VisualObject

enum class CompositeType {
    UNION,
    INTERSECT,
    SUBTRACT
}

class Composite(
    parent: VisualObject?,
    val first: VisualObject,
    val second: VisualObject,
    val type: CompositeType = CompositeType.UNION,
    meta: Meta = EmptyMeta
) : DisplayLeaf(parent, meta)

fun VisualGroup.composite(type: CompositeType, builder: VisualGroup.() -> Unit): Composite {
    val group = VisualGroup().apply(builder)
    val children = group.toList()
    if (children.size != 2) error("Composite requires exactly two children")
    return Composite(this, children[0], children[1], type, group.properties.seal()).also { add(it) }
}

fun VisualGroup.union(builder: VisualGroup.() -> Unit) =
    composite(CompositeType.UNION,builder)

fun VisualGroup.subtract(builder: VisualGroup.() -> Unit) =
    composite(CompositeType.SUBTRACT,builder)

fun VisualGroup.intersect(builder: VisualGroup.() -> Unit) =
    composite(CompositeType.INTERSECT,builder)