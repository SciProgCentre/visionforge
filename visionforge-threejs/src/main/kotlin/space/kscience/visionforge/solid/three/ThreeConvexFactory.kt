package space.kscience.visionforge.solid.three

import info.laht.threekt.external.geometries.ConvexBufferGeometry
import space.kscience.visionforge.solid.Convex

public object ThreeConvexFactory : MeshThreeFactory<Convex>(Convex::class) {
    override fun buildGeometry(obj: Convex): ConvexBufferGeometry {
        val vectors = obj.points.map { it.toVector() }.toTypedArray()
        return ConvexBufferGeometry(vectors)
    }
}