package hep.dataforge.vis.spatial.three

import hep.dataforge.vis.spatial.Convex
import info.laht.threekt.external.geometries.ConvexBufferGeometry

object ThreeConvexFactory : MeshThreeFactory<Convex>(Convex::class) {
    override fun buildGeometry(obj: Convex): ConvexBufferGeometry {
        val vectors = obj.points.map { it.asVector() }.toTypedArray()
        return ConvexBufferGeometry(vectors)
    }
}