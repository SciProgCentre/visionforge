package space.kscience.visionforge.solid.three

import space.kscience.visionforge.solid.Box
import space.kscience.visionforge.solid.detail
import three.geometries.BoxGeometry

public object ThreeBoxFactory : ThreeMeshFactory<Box>(Box::class) {
    override fun buildGeometry(obj: Box): BoxGeometry =
        obj.detail?.let { detail ->
            BoxGeometry(obj.xSize, obj.ySize, obj.zSize, detail, detail, detail)
        } ?: BoxGeometry(obj.xSize, obj.ySize, obj.zSize)
}