package hep.dataforge.vision.solid.three

import hep.dataforge.vision.solid.Convex
import info.laht.threekt.external.geometries.ConvexBufferGeometry

public object ThreeConvexFactory : MeshThreeFactory<Convex>(Convex::class) {
    override fun buildGeometry(obj: Convex): ConvexBufferGeometry {
        val vectors = obj.points.map { it.toVector() }.toTypedArray()
        return ConvexBufferGeometry(vectors)
    }
}