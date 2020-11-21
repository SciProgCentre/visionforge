package hep.dataforge.vision.solid.three

import hep.dataforge.vision.solid.Box
import hep.dataforge.vision.solid.detail
import info.laht.threekt.geometries.BoxBufferGeometry

public object ThreeBoxFactory : MeshThreeFactory<Box>(Box::class) {
    override fun buildGeometry(obj: Box): BoxBufferGeometry =
        obj.detail?.let { detail ->
            BoxBufferGeometry(obj.xSize, obj.ySize, obj.zSize, detail, detail, detail)
        } ?: BoxBufferGeometry(obj.xSize, obj.ySize, obj.zSize)
}