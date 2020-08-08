package hep.dataforge.vision.solid.three

import hep.dataforge.vision.solid.ConeSegment
import hep.dataforge.vision.solid.detail
import info.laht.threekt.core.BufferGeometry
import info.laht.threekt.geometries.CylinderBufferGeometry
import kotlin.math.PI
import kotlin.math.pow

object ThreeCylinderFactory : MeshThreeFactory<ConeSegment>(ConeSegment::class) {
    override fun buildGeometry(obj: ConeSegment): BufferGeometry {
        val cylinder =  obj.detail?.let {
            val segments = it.toDouble().pow(0.5).toInt()
            CylinderBufferGeometry(
                radiusTop = obj.upperRadius,
                radiusBottom = obj.radius,
                height = obj.height,
                radialSegments = segments,
                heightSegments = segments,
                openEnded = false,
                thetaStart = obj.startAngle,
                thetaLength = obj.angle
            )
        } ?: CylinderBufferGeometry(
            radiusTop = obj.upperRadius,
            radiusBottom = obj.radius,
            height = obj.height,
            openEnded = false,
            thetaStart = obj.startAngle,
            thetaLength = obj.angle
        )
        return cylinder.rotateX(PI/2)
    }
}