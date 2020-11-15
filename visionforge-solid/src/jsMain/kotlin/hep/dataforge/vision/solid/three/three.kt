package hep.dataforge.vision.solid.three

import hep.dataforge.meta.MetaItem
import hep.dataforge.meta.float
import hep.dataforge.meta.get
import hep.dataforge.meta.node
import hep.dataforge.vision.solid.*
import info.laht.threekt.core.*
import info.laht.threekt.external.controls.OrbitControls
import info.laht.threekt.materials.Material
import info.laht.threekt.math.Euler
import info.laht.threekt.math.Vector3
import info.laht.threekt.objects.Mesh
import info.laht.threekt.textures.Texture
import kotlin.math.PI

public val Solid.euler: Euler get() = Euler(rotationX, rotationY, rotationZ, rotationOrder.name)

public val MetaItem<*>.vector: Vector3 get() = Vector3(node["x"].float ?: 0f, node["y"].float ?: 0f, node["z"].float ?: 0f)

public fun Geometry.toBufferGeometry(): BufferGeometry = BufferGeometry().apply { fromGeometry(this@toBufferGeometry) }

internal fun Double.toRadians() = this * PI / 180

public fun CSG.toGeometry(): Geometry {
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
            val fc = Face3(v0, v0 + j - 2, v0 + j - 1, World.ZERO)
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

internal fun Any.dispose() {
    when (this) {
        is Geometry -> dispose()
        is BufferGeometry -> dispose()
        is DirectGeometry -> dispose()
        is Material -> dispose()
        is Mesh -> {
            geometry.dispose()
            material.dispose()
        }
        is OrbitControls -> dispose()
        is Texture -> dispose()
    }
}

public fun Layers.check(layer: Int): Boolean = (mask shr(layer) and 0x00000001) > 0