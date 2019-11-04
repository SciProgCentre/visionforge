@file:UseSerializers(Point3DSerializer::class)
package hep.dataforge.vis.spatial

import hep.dataforge.context.Context
import hep.dataforge.io.serialization.ConfigSerializer
import hep.dataforge.io.toMeta
import hep.dataforge.meta.Config
import hep.dataforge.meta.Meta
import hep.dataforge.meta.float
import hep.dataforge.meta.get
import hep.dataforge.vis.common.AbstractVisualObject
import hep.dataforge.vis.common.VisualFactory
import hep.dataforge.vis.common.VisualObject
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlin.reflect.KClass

@Serializable
class Box(
    val xSize: Float,
    val ySize: Float,
    val zSize: Float
) : AbstractVisualObject(), VisualObject3D, Shape {

    override var position: Point3D? = null
    override var rotation: Point3D? = null
    override var scale: Point3D? = null

    @Serializable(ConfigSerializer::class)
    override var properties: Config? = null

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

    override fun toMeta(): Meta  = Visual3DPlugin.json.toJson(serializer(), this).toMeta()

    companion object : VisualFactory<Box> {
        const val TYPE = "geometry.3d.box"

        override val type: KClass<Box> get() = Box::class

        override fun invoke(context: Context, parent: VisualObject?, meta: Meta): Box = Box(
            meta["xSize"].float!!,
            meta["ySize"].float!!,
            meta["zSize"].float!!
        ).apply {
            update(meta)
        }
    }
}

inline fun VisualGroup3D.box(
    xSize: Number,
    ySize: Number,
    zSize: Number,
    name: String = "",
    action: Box.() -> Unit = {}
) = Box(xSize.toFloat(), ySize.toFloat(), zSize.toFloat()).apply(action).also { set(name, it) }