package hep.dataforge.vis.spatial

import hep.dataforge.meta.EmptyMeta
import hep.dataforge.meta.Meta
import hep.dataforge.vis.common.VisualGroup
import hep.dataforge.vis.common.DisplayLeaf
import hep.dataforge.vis.common.VisualObject
import hep.dataforge.vis.common.double

class Box(parent: VisualObject?, meta: Meta) : DisplayLeaf(parent, meta), Shape {
    var xSize by double(100.0)
    var ySize by double(100.0)
    var zSize by double(100.0)

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

    companion object {
        const val TYPE = "geometry.3d.box"

    }
}

fun VisualGroup.box(meta: Meta = EmptyMeta, action: Box.() -> Unit = {}) =
    Box(this, meta).apply(action).also { add(it) }

fun VisualGroup.box(xSize: Number, ySize: Number, zSize: Number, meta: Meta = EmptyMeta, action: Box.() -> Unit = {}) =
    Box(this, meta).apply(action).apply{
        this.xSize = xSize.toDouble()
        this.ySize = ySize.toDouble()
        this.zSize = zSize.toDouble()
    }.also { add(it) }