package hep.dataforge.vis.spatial.three

import hep.dataforge.vis.spatial.Sphere
import hep.dataforge.vis.spatial.detail
import info.laht.threekt.core.BufferGeometry
import info.laht.threekt.geometries.SphereBufferGeometry
import kotlin.math.pow

object ThreeSphereFactory : MeshThreeFactory<Sphere>(Sphere::class) {
    override fun buildGeometry(obj: Sphere): BufferGeometry {
        return obj.detail?.let {
            val segments = it.toDouble().pow(0.5).toInt()
            SphereBufferGeometry(
                radius = obj.radius,
                phiStart = obj.phiStart,
                phiLength = obj.phi,
                thetaStart = obj.thetaStart,
                thetaLength = obj.theta,
                widthSegments = segments,
                heightSegments = segments
            )
        }?: SphereBufferGeometry(
            radius = obj.radius,
            phiStart = obj.phiStart,
            phiLength = obj.phi,
            thetaStart = obj.thetaStart,
            thetaLength = obj.theta
        )
    }
}