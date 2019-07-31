package hep.dataforge.vis.spatial

import hep.dataforge.meta.isEmpty
import hep.dataforge.meta.update
import hep.dataforge.vis.common.VisualObject

enum class CompositeType {
    UNION,
    INTERSECT,
    SUBTRACT
}

open class Composite(
    parent: VisualObject?,
    val first: VisualObject3D,
    val second: VisualObject3D,
    val compositeType: CompositeType = CompositeType.UNION
) : VisualLeaf3D(parent)

fun VisualGroup3D.composite(
    type: CompositeType,
    name: String? = null,
    builder: VisualGroup3D.() -> Unit
): Composite {
    val group = VisualGroup3D().apply(builder)
    val children = group.filterIsInstance<VisualObject3D>()
    if (children.size != 2) error("Composite requires exactly two children")
    return Composite(this, children[0], children[1], type).also {
        if (!group.config.isEmpty()) {
            it.config.update(group.config)
        }
        it.position = group.position
        it.rotation = group.rotation
        it.scale = group.scale
        it.material = group.material
        set(name, it)
    }
}

fun VisualGroup3D.union(name: String? = null, builder: VisualGroup3D.() -> Unit) =
    composite(CompositeType.UNION, name, builder = builder)

fun VisualGroup3D.subtract(name: String? = null, builder: VisualGroup3D.() -> Unit) =
    composite(CompositeType.SUBTRACT, name, builder = builder)

fun VisualGroup3D.intersect(name: String? = null, builder: VisualGroup3D.() -> Unit) =
    composite(CompositeType.INTERSECT, name, builder = builder)