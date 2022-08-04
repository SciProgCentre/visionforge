package space.kscience.visionforge.solid

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.dataforge.meta.isEmpty
import space.kscience.dataforge.names.Name
import space.kscience.visionforge.*

public enum class CompositeType {
    GROUP, // Dumb sum of meshes
    UNION, //CSG union
    INTERSECT,
    SUBTRACT
}

@Serializable
@SerialName("solid.composite")
public class Composite(
    public val compositeType: CompositeType,
    public val first: Solid,
    public val second: Solid,
) : SolidBase(), VisionPropertyContainer<Composite>

@VisionBuilder
public inline fun VisionContainerBuilder<Solid>.composite(
    type: CompositeType,
    name: String? = null,
    @VisionBuilder builder: SolidGroup.() -> Unit,
): Composite {
    val group = SolidGroup(builder)
    val children = group.items.values.toList()
    if (children.size != 2) {
        error("Composite requires exactly two children, but found ${children.size}")
    }
    val res = Composite(type, children[0], children[1])

    res.setProperty(Name.EMPTY, group.getProperty(Name.EMPTY))

    set(name, res)
    return res
}

/**
 * A smart form of [Composite] that in case of [CompositeType.GROUP] creates a static group instead
 */
@VisionBuilder
public fun SolidGroup.smartComposite(
    type: CompositeType,
    name: String? = null,
    @VisionBuilder builder: SolidGroup.() -> Unit,
): Solid = if (type == CompositeType.GROUP) {
    val group = SolidGroup(builder)
    if (name == null && group.meta.isEmpty()) {
        //append directly to group if no properties are defined
        group.items.forEach { (_, value) ->
            value.parent = null
            children.static(value)
        }
        this
    } else {
        children[name] = group
        group
    }
} else {
    children.composite(type, name, builder)
}

@VisionBuilder
public inline fun VisionContainerBuilder<Solid>.union(
    name: String? = null,
    builder: SolidGroup.() -> Unit,
): Composite = composite(CompositeType.UNION, name, builder = builder)

@VisionBuilder
public inline fun VisionContainerBuilder<Solid>.subtract(
    name: String? = null,
    builder: SolidGroup.() -> Unit,
): Composite = composite(CompositeType.SUBTRACT, name, builder = builder)

@VisionBuilder
public inline fun VisionContainerBuilder<Solid>.intersect(
    name: String? = null,
    builder: SolidGroup.() -> Unit,
): Composite = composite(CompositeType.INTERSECT, name, builder = builder)