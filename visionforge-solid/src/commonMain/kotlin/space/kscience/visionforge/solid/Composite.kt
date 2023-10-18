package space.kscience.visionforge.solid

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.dataforge.names.Name
import space.kscience.visionforge.MutableVisionContainer
import space.kscience.visionforge.VisionBuilder
import space.kscience.visionforge.setChild
import space.kscience.visionforge.static

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

@VisionBuilder
public inline fun MutableVisionContainer<Solid>.composite(
    type: CompositeType,
    name: String? = null,
    @VisionBuilder builder: SolidGroup.() -> Unit,
): Composite {
    val group = SolidGroup().apply(builder)
    val children = group.items.values.toList()
    if (children.size != 2) {
        error("Composite requires exactly two children, but found ${children.size}")
    }
    val res = Composite(type, children[0], children[1])

    res.properties.setMeta(Name.EMPTY, group.properties.own)

    setChild(name, res)
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
    val group = SolidGroup().apply(builder)
    if (name == null && group.properties.own == null) {
        //append directly to group if no properties are defined
        group.items.forEach { (_, value) ->
            value.parent = null
            children.static(value)
        }
        this
    } else {
        children.setChild(name, group)
        group
    }
} else {
    children.composite(type, name, builder)
}

@VisionBuilder
public inline fun MutableVisionContainer<Solid>.union(
    name: String? = null,
    builder: SolidGroup.() -> Unit,
): Composite = composite(CompositeType.UNION, name, builder = builder)

@VisionBuilder
public inline fun MutableVisionContainer<Solid>.subtract(
    name: String? = null,
    builder: SolidGroup.() -> Unit,
): Composite = composite(CompositeType.SUBTRACT, name, builder = builder)

@VisionBuilder
public inline fun MutableVisionContainer<Solid>.intersect(
    name: String? = null,
    builder: SolidGroup.() -> Unit,
): Composite = composite(CompositeType.INTERSECT, name, builder = builder)