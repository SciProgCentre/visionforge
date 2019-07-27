package hep.dataforge.vis.spatial

import hep.dataforge.meta.Meta
import hep.dataforge.meta.seal
import hep.dataforge.meta.update
import hep.dataforge.vis.common.VisualGroup
import hep.dataforge.vis.common.VisualObject

enum class CompositeType {
    UNION,
    INTERSECT,
    SUBTRACT
}

open class Composite(
    parent: VisualObject?,
    val first: VisualObject,
    val second: VisualObject,
    val type: CompositeType = CompositeType.UNION,
    meta: Array<out Meta>
) : VisualObject3D(parent, meta)

fun VisualGroup.composite(
    type: CompositeType,
    name: String? = null,
    vararg meta: Meta,
    builder: VisualGroup.() -> Unit
): Composite {
    val group = VisualGroup().apply(builder)
    val children = group.toList()
    if (children.size != 2) error("Composite requires exactly two children")
    val groupMeta = group.properties.seal()
    return Composite(this, children[0], children[1], type, meta).also {
        it.config.update(groupMeta)
        set(name, it)
    }
}

fun VisualGroup.union(name: String? = null, vararg meta: Meta, builder: VisualGroup.() -> Unit) =
    composite(CompositeType.UNION, name, *meta, builder = builder)

fun VisualGroup.subtract(name: String? = null, vararg meta: Meta, builder: VisualGroup.() -> Unit) =
    composite(CompositeType.SUBTRACT, name, *meta, builder = builder)

fun VisualGroup.intersect(name: String? = null, vararg meta: Meta, builder: VisualGroup.() -> Unit) =
    composite(CompositeType.INTERSECT, name, *meta, builder = builder)