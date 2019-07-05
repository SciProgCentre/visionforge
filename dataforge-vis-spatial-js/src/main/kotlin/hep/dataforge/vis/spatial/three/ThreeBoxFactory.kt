package hep.dataforge.vis.spatial.three

import hep.dataforge.vis.spatial.Box
import hep.dataforge.vis.spatial.detail
import info.laht.threekt.geometries.BoxBufferGeometry
import kotlin.math.pow

object ThreeBoxFactory : MeshThreeFactory<Box>(Box::class) {
    override fun buildGeometry(obj: Box) =
        obj.detail?.let {
            val segments = it.toDouble().pow(1.0 / 3.0).toInt()
            BoxBufferGeometry(obj.xSize, obj.ySize, obj.zSize, segments, segments, segments)
        } ?: BoxBufferGeometry(obj.xSize, obj.ySize, obj.zSize)
}