package space.kscience.visionforge.solid.three

import space.kscience.visionforge.solid.Convex
import three.external.geometries.ConvexBufferGeometry

public object ThreeConvexFactory : ThreeMeshFactory<Convex>(Convex::class) {
    override fun buildGeometry(obj: Convex): ConvexBufferGeometry {
        val vectors = obj.points.map { it.toVector() }.toTypedArray()
        return ConvexBufferGeometry(vectors)
    }
}