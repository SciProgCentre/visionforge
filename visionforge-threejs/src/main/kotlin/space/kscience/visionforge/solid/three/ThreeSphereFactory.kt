package space.kscience.visionforge.solid.three

import space.kscience.visionforge.solid.Sphere
import space.kscience.visionforge.solid.detail
import three.core.BufferGeometry
import three.geometries.SphereGeometry

public object ThreeSphereFactory : ThreeMeshFactory<Sphere>(Sphere::class) {
    override fun buildGeometry(obj: Sphere): BufferGeometry {
        return obj.detail?.let { detail ->
            SphereGeometry(
                radius = obj.radius,
                phiStart = obj.phiStart,
                phiLength = obj.phi,
                thetaStart = obj.thetaStart,
                thetaLength = obj.theta,
                widthSegments = detail,
                heightSegments = detail
            )
        } ?: SphereGeometry(
            radius = obj.radius,
            phiStart = obj.phiStart,
            phiLength = obj.phi,
            thetaStart = obj.thetaStart,
            thetaLength = obj.theta
        )
    }
}