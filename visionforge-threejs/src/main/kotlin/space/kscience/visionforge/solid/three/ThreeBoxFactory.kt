package space.kscience.visionforge.solid.three

import info.laht.threekt.geometries.BoxGeometry
import space.kscience.visionforge.solid.Box
import space.kscience.visionforge.solid.detail

public object ThreeBoxFactory : MeshThreeFactory<Box>(Box::class) {
    override fun buildGeometry(obj: Box): BoxGeometry =
        obj.detail?.let { detail ->
            BoxGeometry(obj.xSize, obj.ySize, obj.zSize, detail, detail, detail)
        } ?: BoxGeometry(obj.xSize, obj.ySize, obj.zSize)
}