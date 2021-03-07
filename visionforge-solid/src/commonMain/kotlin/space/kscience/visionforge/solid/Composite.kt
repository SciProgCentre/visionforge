package space.kscience.visionforge.solid

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.dataforge.meta.update
import space.kscience.dataforge.names.NameToken
import space.kscience.visionforge.*

public enum class CompositeType {
    UNION,
    INTERSECT,
    SUBTRACT
}

@Serializable
@SerialName("solid.composite")
public class Composite(
    public val compositeType: CompositeType,
    public val first: Solid,
    public val second: Solid,
) : SolidBase(), Solid, VisionGroup {

    init {
        first.parent = this
        second.parent = this
    }

    override val children: Map<NameToken, Vision>
        get() = mapOf(NameToken("first") to first, NameToken("second") to second)
}

@VisionBuilder
public inline fun VisionContainerBuilder<Solid>.composite(
    type: CompositeType,
    name: String? = null,
    builder: SolidGroup.() -> Unit,
): Composite {
    val group = SolidGroup().apply(builder)
    val children = group.children.values.filterIsInstance<Solid>()
    if (children.size != 2) error("Composite requires exactly two children")
    return Composite(type, children[0], children[1]).also { composite ->
        composite.configure {
            update(group.meta)
        }
        if (group.position != null) {
            composite.position = group.position
        }
        if (group.rotation != null) {
            composite.rotation = group.rotation
        }
        if (group.scale != null) {
            composite.scale = group.scale
        }
        set(name, composite)
    }
}

@VisionBuilder
public inline fun VisionContainerBuilder<Solid>.union(name: String? = null, builder: SolidGroup.() -> Unit): Composite =
    composite(CompositeType.UNION, name, builder = builder)

@VisionBuilder
public inline fun VisionContainerBuilder<Solid>.subtract(name: String? = null, builder: SolidGroup.() -> Unit): Composite =
    composite(CompositeType.SUBTRACT, name, builder = builder)

@VisionBuilder
public inline fun VisionContainerBuilder<Solid>.intersect(
    name: String? = null,
    builder: SolidGroup.() -> Unit,
): Composite =
    composite(CompositeType.INTERSECT, name, builder = builder)