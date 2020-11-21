package hep.dataforge.vision.solid.three

import hep.dataforge.vision.solid.Convex
import info.laht.threekt.external.geometries.ConvexBufferGeometry
import info.laht.threekt.math.Vector3

public object ThreeConvexFactory : MeshThreeFactory<Convex>(Convex::class) {
    override fun buildGeometry(obj: Convex): ConvexBufferGeometry {
        @Suppress("USELESS_CAST") val vectors = obj.points.toTypedArray() as Array<Vector3>
        return ConvexBufferGeometry(vectors)
    }
}