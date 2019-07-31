package hep.dataforge.vis.spatial

import hep.dataforge.vis.common.VisualObject

class Box(parent: VisualObject?, val xSize: Number, val ySize: Number, val zSize: Number) :
    VisualLeaf3D(parent), Shape {

    //TODO add helper for color configuration
    override fun <T : Any> toGeometry(geometryBuilder: GeometryBuilder<T>) {
        val dx = xSize.toDouble() / 2
        val dy = ySize.toDouble() / 2
        val dz = zSize.toDouble() / 2
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

inline fun VisualGroup3D.box(
    xSize: Number,
    ySize: Number,
    zSize: Number,
    name: String? = null,
    action: Box.() -> Unit = {}
) = Box(this, xSize, ySize, zSize).apply(action).also { set(name, it) }