@file:UseSerializers(Point3DSerializer::class)

package hep.dataforge.vision.solid

import hep.dataforge.meta.Config
import hep.dataforge.vision.AbstractVision
import hep.dataforge.vision.MutableVisionGroup
import hep.dataforge.vision.VisionContainerBuilder
import hep.dataforge.vision.set
import hep.dataforge.vision.solid.Solid.Companion.solidEquals
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
@SerialName("solid.box")
public class Box(
    public val xSize: Float,
    public val ySize: Float,
    public val zSize: Float
) : AbstractSolid(), GeometrySolid {

    //TODO add helper for color configuration
    override fun <T : Any> toGeometry(geometryBuilder: GeometryBuilder<T>) {
        val dx = xSize / 2
        val dy = ySize / 2
        val dz = zSize / 2
        val node1 = Point3D(-dx, -dy, -dz)
        val node2 = Point3D(dx, -dy, -dz)
        val node3 = Point3D(dx, dy, -dz)
        val node4 = Point3D(-dx, dy, -dz)
        val node5 = Point3D(-dx, -dy, dz)
        val node6 = Point3D(dx, -dy, dz)
        val node7 = Point3D(dx, dy, dz)
        val node8 = Point3D(-dx, dy, dz)
        geometryBuilder.face4(node1, node4, node3, node2)
        geometryBuilder.face4(node1, node2, node6, node5)
        geometryBuilder.face4(node2, node3, node7, node6)
        geometryBuilder.face4(node4, node8, node7, node3)
        geometryBuilder.face4(node1, node5, node8, node4)
        geometryBuilder.face4(node8, node5, node6, node7)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Box

        if (xSize != other.xSize) return false
        if (ySize != other.ySize) return false
        if (zSize != other.zSize) return false

        return solidEquals(this, other)
    }

    override fun hashCode(): Int {
        var result = xSize.hashCode()
        result = 31 * result + ySize.hashCode()
        result = 31 * result + zSize.hashCode()
        return 31 * result + Solid.solidHashCode(this)
    }


    public companion object {

    }
}

public inline fun VisionContainerBuilder<Solid>.box(
    xSize: Number,
    ySize: Number,
    zSize: Number,
    name: String = "",
    action: Box.() -> Unit = {}
): Box = Box(xSize.toFloat(), ySize.toFloat(), zSize.toFloat()).apply(action).also { set(name, it) }