@file:UseSerializers(Point3DSerializer::class)

package hep.dataforge.vision.solid

import hep.dataforge.meta.Config
import hep.dataforge.vision.AbstractVision
import hep.dataforge.vision.MutableVisionGroup
import hep.dataforge.vision.VisionContainerBuilder
import hep.dataforge.vision.set
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
@SerialName("solid.convex")
public class Convex(public val points: List<Point3D>) : AbstractSolid(), Solid

public inline fun VisionContainerBuilder<Solid>.convex(name: String = "", action: ConvexBuilder.() -> Unit = {}): Convex =
    ConvexBuilder().apply(action).build().also { set(name, it) }

public class ConvexBuilder {
    private val points = ArrayList<Point3D>()

    public fun point(x: Number, y: Number, z: Number) {
        points.add(Point3D(x, y, z))
    }

    public fun build(): Convex {
        return Convex(points)
    }
}