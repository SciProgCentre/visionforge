package space.kscience.visionforge.solid.three

import info.laht.threekt.geometries.BoxBufferGeometry
import space.kscience.visionforge.solid.Box
import space.kscience.visionforge.solid.detail

public object ThreeBoxFactory : MeshThreeFactory<Box>(Box::class) {
    override fun buildGeometry(obj: Box): BoxBufferGeometry =
        obj.detail?.let { detail ->
            BoxBufferGeometry(obj.xSize, obj.ySize, obj.zSize, detail, detail, detail)
        } ?: BoxBufferGeometry(obj.xSize, obj.ySize, obj.zSize)
}