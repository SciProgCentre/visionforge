package hep.dataforge.vis.spatial.three

import hep.dataforge.meta.MetaItem
import hep.dataforge.meta.float
import hep.dataforge.meta.get
import hep.dataforge.meta.node
import hep.dataforge.vis.spatial.*
import info.laht.threekt.core.BufferGeometry
import info.laht.threekt.core.Face3
import info.laht.threekt.core.Geometry
import info.laht.threekt.core.Object3D
import info.laht.threekt.math.Euler
import info.laht.threekt.math.Vector3

/**
 * Utility methods for three.kt.
 * TODO move to three project
 */

@Suppress("FunctionName")
fun Group(children: Collection<Object3D>) = info.laht.threekt.objects.Group().apply {
    children.forEach { this.add(it) }
}

val VisualObject3D.euler get() = Euler(rotationX, rotationY, rotationZ, rotationOrder.name)

val MetaItem<*>.vector get() = Vector3(node["x"].float ?: 0f, node["y"].float ?: 0f, node["z"].float ?: 0f)

fun Geometry.toBufferGeometry(): BufferGeometry = BufferGeometry().apply { fromGeometry(this@toBufferGeometry) }

fun CSG.toGeometry(): Geometry {
    val geom = Geometry()

    val vertices = ArrayList<Vector3>()
    val faces = ArrayList<Face3>()

    for (polygon in polygons) {
        val v0 = vertices.size
        val pvs = polygon.vertices

        for (pv in pvs) {
            vertices.add(Vector3().copy(pv.pos))
        }

        for (j in 3..polygon.vertices.size) {
            val fc = Face3(v0, v0 + j - 2, v0 + j - 1, zero)
            fc.vertexNormals = arrayOf(
                Vector3().copy(pvs[0].normal),
                Vector3().copy(pvs[j - 2].normal),
                Vector3().copy(pvs[j - 1].normal)
            )

            fc.normal = Vector3().copy(polygon.plane.normal)
            faces.add(fc)
        }
    }
    geom.vertices = vertices.toTypedArray()
    geom.faces = faces.toTypedArray()
//    val inv: Matrix4 = Matrix4().apply { getInverse(toMatrix) }
//    geom.applyMatrix(toMatrix)
    geom.verticesNeedUpdate = true
    geom.elementsNeedUpdate = true
    geom.normalsNeedUpdate = true
    geom.computeBoundingSphere()
    geom.computeBoundingBox()
    return geom
}