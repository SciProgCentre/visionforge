@file:UseSerializers(Point3DSerializer::class)

package hep.dataforge.vis.spatial

import hep.dataforge.io.serialization.ConfigSerializer
import hep.dataforge.meta.Config
import hep.dataforge.meta.update
import hep.dataforge.vis.common.AbstractVisualObject
import hep.dataforge.vis.common.set
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

enum class CompositeType {
    UNION,
    INTERSECT,
    SUBTRACT
}

@Serializable
class Composite(
    val compositeType: CompositeType,
    val first: VisualObject3D,
    val second: VisualObject3D
) : AbstractVisualObject(), VisualObject3D {

    init {
        first.parent = this
        second.parent = this
    }

    override var position: Point3D? = null
    override var rotation: Point3D? = null
    override var scale: Point3D? = null

    @Serializable(ConfigSerializer::class)
    override var properties: Config? = null
}

inline fun VisualGroup3D.composite(
    type: CompositeType,
    name: String = "",
    builder: VisualGroup3D.() -> Unit
): Composite {
    val group = VisualGroup3D().apply(builder)
    val children = group.filterIsInstance<VisualObject3D>()
    if (children.size != 2) error("Composite requires exactly two children")
    return Composite(type, children[0], children[1]).also {
        it.config.update(group.config)
        //it.material = group.material

        if(group.position!=null) {
            it.position = group.position
        }
        if(group.rotation!=null) {
            it.rotation = group.rotation
        }
        if(group.scale!=null) {
            it.scale = group.scale
        }
        set(name, it)
    }
}

fun VisualGroup3D.union(name: String = "", builder: VisualGroup3D.() -> Unit) =
    composite(CompositeType.UNION, name, builder = builder)

fun VisualGroup3D.subtract(name: String = "", builder: VisualGroup3D.() -> Unit) =
    composite(CompositeType.SUBTRACT, name, builder = builder)

fun VisualGroup3D.intersect(name: String = "", builder: VisualGroup3D.() -> Unit) =
    composite(CompositeType.INTERSECT, name, builder = builder)