@file:UseSerializers(Point3DSerializer::class)

package hep.dataforge.vis.spatial

import hep.dataforge.io.serialization.ConfigSerializer
import hep.dataforge.meta.Config
import hep.dataforge.meta.MetaBuilder
import hep.dataforge.vis.common.AbstractVisualObject
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
class Convex(val points: List<Point3D>) : AbstractVisualObject(), VisualObject3D {

    @Serializable(ConfigSerializer::class)
    override var properties: Config? = null

    override var position: Point3D? = null
    override var rotation: Point3D? = null
    override var scale: Point3D? = null

    override fun MetaBuilder.updateMeta() {
        "points" put {
            "point" put points.map { it.toMeta() }
        }
        updatePosition()
    }

    companion object {
        const val TYPE = "geometry.3d.convex"
    }
}

inline fun VisualGroup3D.convex(name: String = "", action: ConvexBuilder.() -> Unit = {}) =
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