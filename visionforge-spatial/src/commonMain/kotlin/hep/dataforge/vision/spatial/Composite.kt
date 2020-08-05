@file:UseSerializers(Point3DSerializer::class)

package hep.dataforge.vision.spatial

import hep.dataforge.meta.Config
import hep.dataforge.meta.update
import hep.dataforge.names.NameToken
import hep.dataforge.vision.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

enum class CompositeType {
    UNION,
    INTERSECT,
    SUBTRACT
}

@Serializable
@SerialName("3d.composite")
class Composite(
    val compositeType: CompositeType,
    val first: VisualObject3D,
    val second: VisualObject3D
) : AbstractVisualObject(), VisualObject3D, VisionGroup {

    init {
        first.parent = this
        second.parent = this
    }

    override var position: Point3D? = null
    override var rotation: Point3D? = null
    override var scale: Point3D? = null

    override var ownProperties: Config? = null

    override val children: Map<NameToken, Vision>
        get() = mapOf(NameToken("first") to first, NameToken("second") to second)

    override val styleSheet: StyleSheet?
        get() = null
}

inline fun MutableVisionGroup.composite(
    type: CompositeType,
    name: String = "",
    builder: VisionGroup3D.() -> Unit
): Composite {
    val group = VisionGroup3D().apply(builder)
    val children = group.children.values.filterIsInstance<VisualObject3D>()
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

inline fun MutableVisionGroup.union(name: String = "", builder: VisionGroup3D.() -> Unit) =
    composite(CompositeType.UNION, name, builder = builder)

inline fun MutableVisionGroup.subtract(name: String = "", builder: VisionGroup3D.() -> Unit) =
    composite(CompositeType.SUBTRACT, name, builder = builder)

inline fun MutableVisionGroup.intersect(name: String = "", builder: VisionGroup3D.() -> Unit) =
    composite(CompositeType.INTERSECT, name, builder = builder)