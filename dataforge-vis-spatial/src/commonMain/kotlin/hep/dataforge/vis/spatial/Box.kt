package hep.dataforge.vis.spatial

import hep.dataforge.meta.EmptyMeta
import hep.dataforge.meta.Meta
import hep.dataforge.vis.common.DisplayLeaf
import hep.dataforge.vis.common.DisplayObject
import hep.dataforge.vis.common.DisplayObjectList
import hep.dataforge.vis.common.double

class Box(parent: DisplayObject?, meta: Meta) : DisplayLeaf(parent, meta), Shape {
    var xSize by double(1.0)
    var ySize by double(1.0)
    var zSize by double(1.0)

    //TODO add helper for color configuration

    override fun <T : Any> GeometryBuilder<T>.buildGeometry() {
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
        face4(node1, node4, node3, node2, Point3D(0, 0, -1))
        face4(node1, node2, node6, node5, Point3D(0, -1, 0))
        face4(node2, node3, node7, node6, Point3D(1, 0, 0))
        face4(node4, node8, node7, node3, Point3D(0, 1, 0))
        face4(node1, node5, node8, node4, Point3D(-1, 0, 0))
        face4(node8, node5, node6, node7, Point3D(0, 0, 1))
    }

    companion object {
        const val TYPE = "geometry.3d.box"
    }
}

fun DisplayObjectList.box(meta: Meta = EmptyMeta, action: Box.() -> Unit = {}) =
    Box(this, meta).apply(action).also { addChild(it) }