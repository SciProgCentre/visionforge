
package hep.dataforge.vision.solid

import hep.dataforge.meta.update
import hep.dataforge.names.NameToken
import hep.dataforge.vision.Vision
import hep.dataforge.vision.VisionContainerBuilder
import hep.dataforge.vision.VisionGroup
import hep.dataforge.vision.set
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
    public val second: Solid
) : SolidBase(), Solid, VisionGroup {

    init {
        first.parent = this
        second.parent = this
    }

    override val children: Map<NameToken, Vision>
        get() = mapOf(NameToken("first") to first, NameToken("second") to second)
}

public inline fun VisionContainerBuilder<Solid>.composite(
    type: CompositeType,
    name: String = "",
    builder: SolidGroup.() -> Unit
): Composite {
    val group = SolidGroup().apply(builder)
    val children = group.children.values.filterIsInstance<Solid>()
    if (children.size != 2) error("Composite requires exactly two children")
    return Composite(type, children[0], children[1]).also {
        it.config.update(group.config)
        //it.material = group.material

        if (group.position != null) {
            it.position = group.position
        }
        if (group.rotation != null) {
            it.rotation = group.rotation
        }
        if (group.scale != null) {
            it.scale = group.scale
        }
        set(name, it)
    }
}

public inline fun VisionContainerBuilder<Solid>.union(name: String = "", builder: SolidGroup.() -> Unit): Composite =
    composite(CompositeType.UNION, name, builder = builder)

public inline fun VisionContainerBuilder<Solid>.subtract(name: String = "", builder: SolidGroup.() -> Unit): Composite =
    composite(CompositeType.SUBTRACT, name, builder = builder)

public inline fun VisionContainerBuilder<Solid>.intersect(name: String = "", builder: SolidGroup.() -> Unit): Composite =
    composite(CompositeType.INTERSECT, name, builder = builder)