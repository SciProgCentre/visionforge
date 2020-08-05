package hep.dataforge.vision.spatial.three

import hep.dataforge.vision.spatial.Convex
import info.laht.threekt.external.geometries.ConvexBufferGeometry
import info.laht.threekt.math.Vector3

object ThreeConvexFactory : MeshThreeFactory<Convex>(Convex::class) {
    override fun buildGeometry(obj: Convex): ConvexBufferGeometry {
        @Suppress("USELESS_CAST") val vectors = obj.points.toTypedArray() as Array<Vector3>
        return ConvexBufferGeometry(vectors)
    }
}