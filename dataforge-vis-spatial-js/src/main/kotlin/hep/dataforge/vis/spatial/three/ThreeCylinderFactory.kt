package hep.dataforge.vis.spatial.three

import hep.dataforge.vis.spatial.Cylinder
import hep.dataforge.vis.spatial.detail
import info.laht.threekt.core.BufferGeometry
import info.laht.threekt.geometries.CylinderBufferGeometry
import kotlin.math.pow

object ThreeCylinderFactory : MeshThreeFactory<Cylinder>(Cylinder::class) {
    override fun buildGeometry(obj: Cylinder): BufferGeometry {
        return obj.detail?.let {
            val segments = it.toDouble().pow(0.5).toInt()
            CylinderBufferGeometry(
                radiusTop = obj.upperRadius!!,
                radiusBottom = obj.radius!!,
                height = obj.height!!,
                radialSegments = segments,
                heightSegments = segments,
                openEnded = false,
                thetaStart = obj.startAngle,
                thetaLength = obj.angle
            )
        } ?: CylinderBufferGeometry(
            radiusTop = obj.upperRadius!!,
            radiusBottom = obj.radius!!,
            height = obj.height!!,
            openEnded = false,
            thetaStart = obj.startAngle,
            thetaLength = obj.angle
        )
    }
}