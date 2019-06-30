package hep.dataforge.vis.spatial

import hep.dataforge.meta.Meta
import hep.dataforge.meta.get
import hep.dataforge.meta.int
import info.laht.threekt.core.Face3
import info.laht.threekt.core.Geometry
import info.laht.threekt.math.Color
import info.laht.threekt.math.Vector3

class ThreeGeometryBuilder : GeometryBuilder<Geometry> {

    private val vertices = ArrayList<Point3D>()
    private val faces = ArrayList<Face3>()

    private fun append(vertex: Point3D): Int {
        val index = vertices.indexOf(vertex)
        return if (index > 0) {
            index
        } else {
            vertices.add(vertex)
            vertices.size - 1
        }
    }

    override fun face(vertex1: Point3D, vertex2: Point3D, vertex3: Point3D, normal: Point3D?, meta: Meta) {
        val materialIndex = meta["materialIndex"].int ?: 0
        val color = meta["color"]?.color() ?: Color()
        faces.add(
            Face3(
                append(vertex1),
                append(vertex2),
                append(vertex3),
                normal?.asVector() ?: Vector3(0, 0, 0),
                color,
                materialIndex
            )
        )
    }

    override fun build(): Geometry {
        return Geometry().apply {
            vertices = this@ThreeGeometryBuilder.vertices.map { it.asVector() }.toTypedArray()
            faces = this@ThreeGeometryBuilder.faces.toTypedArray()
        }
    }
}