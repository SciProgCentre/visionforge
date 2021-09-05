package space.kscience.visionforge.solid

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.dataforge.meta.update
import space.kscience.visionforge.VisionBuilder
import space.kscience.visionforge.VisionContainerBuilder
import space.kscience.visionforge.VisionPropertyContainer
import space.kscience.visionforge.set

public enum class CompositeType {
    SUM, // Dumb sum of meshes
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
    builder: SolidGroup.() -> Unit,
): Composite {
    val group = SolidGroup().apply(builder)
    val children = group.children.values.filterIsInstance<Solid>()
    if (children.size != 2) error("Composite requires exactly two children, but found ${children.size}")
    val res = Composite(type, children[0], children[1])

    res.meta.update(group.meta)

    if (group.position != null) {
        res.position = group.position
    }
    if (group.rotation != null) {
        res.rotation = group.rotation
    }
    if (group.scale != null) {
        res.scale = group.scale
    }

    set(name, res)
    return res
}

@VisionBuilder
public inline fun VisionContainerBuilder<Solid>.union(
    name: String? = null,
    builder: SolidGroup.() -> Unit
): Composite = composite(CompositeType.UNION, name, builder = builder)

@VisionBuilder
public inline fun VisionContainerBuilder<Solid>.subtract(
    name: String? = null,
    builder: SolidGroup.() -> Unit
): Composite = composite(CompositeType.SUBTRACT, name, builder = builder)

@VisionBuilder
public inline fun VisionContainerBuilder<Solid>.intersect(
    name: String? = null,
    builder: SolidGroup.() -> Unit,
): Composite = composite(CompositeType.INTERSECT, name, builder = builder)