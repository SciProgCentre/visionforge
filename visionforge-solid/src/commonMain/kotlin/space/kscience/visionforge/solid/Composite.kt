package space.kscience.visionforge.solid

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.dataforge.meta.isEmpty
import space.kscience.dataforge.meta.update
import space.kscience.visionforge.MutableVisionContainer

/**
 * Types of composite solids
 */
public enum class CompositeType {
    GROUP, // Dumb sum of meshes
    UNION, //CSG union
    INTERSECT,
    SUBTRACT
}

/**
 * A CSG-based composite solid
 */
@Serializable
@SerialName("solid.composite")
public class Composite(
    public val compositeType: CompositeType,
    public val first: Solid,
    public val second: Solid,
) : SolidBase<Composite>()

public inline fun MutableVisionContainer<Solid>.composite(
    type: CompositeType,
    name: String? = null,
    builder: SolidGroup.() -> Unit,
): Composite {
    val group = SolidGroup().apply(builder)
    val children = group.visions.values.toList()
    if (children.size != 2) {
        error("Composite requires exactly two children, but found ${children.size}")
    }
    val res = Composite(type, children[0], children[1])

    res.properties.update(group.properties)

    setVision(SolidGroup.inferNameFor(name, res), res)
    return res
}

/**
 * A smart form of [Composite] that in case of [CompositeType.GROUP] creates a static group instead
 */

public fun SolidGroup.smartComposite(
    type: CompositeType,
    name: String? = null,
    builder: SolidGroup.() -> Unit,
): Solid = if (type == CompositeType.GROUP) {
    val group = SolidGroup().apply(builder)
    if (name == null && group.properties.isEmpty()) {
        //append directly to group if no properties are defined
        group.visions.forEach { (_, value) ->
            value.parent = null
            static(value)
        }
        this
    } else {
        setVision(SolidGroup.inferNameFor(name, group), group)
        group
    }
} else {
    composite(type, name, builder)
}

public inline fun MutableVisionContainer<Solid>.union(
    name: String? = null,
    builder: SolidGroup.() -> Unit,
): Composite = composite(CompositeType.UNION, name, builder = builder)

public inline fun MutableVisionContainer<Solid>.subtract(
    name: String? = null,
    builder: SolidGroup.() -> Unit,
): Composite = composite(CompositeType.SUBTRACT, name, builder = builder)

public inline fun MutableVisionContainer<Solid>.intersect(
    name: String? = null,
    builder: SolidGroup.() -> Unit,
): Composite = composite(CompositeType.INTERSECT, name, builder = builder)