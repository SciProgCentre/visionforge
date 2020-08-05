@file:UseSerializers(Point3DSerializer::class)

package hep.dataforge.vision.spatial

import hep.dataforge.meta.Config
import hep.dataforge.vision.AbstractVisualObject
import hep.dataforge.vision.MutableVisualGroup
import hep.dataforge.vision.set
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
@SerialName("3d.convex")
class Convex(val points: List<Point3D>) : AbstractVisualObject(), VisualObject3D {

    override var ownProperties: Config? = null

    override var position: Point3D? = null
    override var rotation: Point3D? = null
    override var scale: Point3D? = null

    companion object {
        const val TYPE = "geometry.3d.convex"
    }
}

inline fun MutableVisualGroup.convex(name: String = "", action: ConvexBuilder.() -> Unit = {}) =
    ConvexBuilder().apply(action).build().also { set(name, it) }

class ConvexBuilder {
    private val points = ArrayList<Point3D>()

    fun point(x: Number, y: Number, z: Number) {
        points.add(Point3D(x, y, z))
    }

    fun build(): Convex {
        return Convex(points)
    }
}