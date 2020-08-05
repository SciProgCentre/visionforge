package hep.dataforge.vision.spatial.three

import hep.dataforge.vision.spatial.Box
import hep.dataforge.vision.spatial.detail
import info.laht.threekt.geometries.BoxBufferGeometry

object ThreeBoxFactory : MeshThreeFactory<Box>(Box::class) {
    override fun buildGeometry(obj: Box) =
        obj.detail?.let { detail ->
            BoxBufferGeometry(obj.xSize, obj.ySize, obj.zSize, detail, detail, detail)
        } ?: BoxBufferGeometry(obj.xSize, obj.ySize, obj.zSize)
}