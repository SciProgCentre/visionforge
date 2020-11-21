package hep.dataforge.vision.solid.three

import hep.dataforge.vision.solid.Composite
import hep.dataforge.vision.solid.CompositeType
import info.laht.threekt.core.BufferGeometry
import info.laht.threekt.objects.Mesh

/**
 * This should be inner, because it uses object builder
 */
public class ThreeCompositeFactory(public val three: ThreePlugin) : MeshThreeFactory<Composite>(Composite::class) {

    override fun buildGeometry(obj: Composite): BufferGeometry {
        val first = three.buildObject3D(obj.first) as? Mesh ?: error("First part of composite is not a mesh")
        first.updateMatrix()
        val second = three.buildObject3D(obj.second) as? Mesh ?: error("Second part of composite is not a mesh")
        second.updateMatrix()
        val firstCSG = CSG.fromMesh(first)
        val secondCSG = CSG.fromMesh(second)
        val resultCSG = when (obj.compositeType) {
            CompositeType.UNION -> firstCSG.union(secondCSG)
            CompositeType.INTERSECT -> firstCSG.intersect(secondCSG)
            CompositeType.SUBTRACT -> firstCSG.subtract(secondCSG)
        }
        return resultCSG.toGeometry().toBufferGeometry()
    }

}