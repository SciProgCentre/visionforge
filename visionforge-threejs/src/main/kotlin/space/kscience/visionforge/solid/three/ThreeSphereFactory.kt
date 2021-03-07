package space.kscience.visionforge.solid.three

import info.laht.threekt.core.BufferGeometry
import info.laht.threekt.geometries.SphereBufferGeometry
import space.kscience.visionforge.solid.Sphere
import space.kscience.visionforge.solid.detail

public object ThreeSphereFactory : MeshThreeFactory<Sphere>(Sphere::class) {
    override fun buildGeometry(obj: Sphere): BufferGeometry {
        return obj.detail?.let {detail ->
            SphereBufferGeometry(
                radius = obj.radius,
                phiStart = obj.phiStart,
                phiLength = obj.phi,
                thetaStart = obj.thetaStart,
                thetaLength = obj.theta,
                widthSegments = detail,
                heightSegments = detail
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