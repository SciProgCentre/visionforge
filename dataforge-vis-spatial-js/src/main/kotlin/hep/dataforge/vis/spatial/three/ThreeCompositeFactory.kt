package hep.dataforge.vis.spatial.three

import hep.dataforge.vis.spatial.Composite
import hep.dataforge.vis.spatial.CompositeType
import info.laht.threekt.core.BufferGeometry
import info.laht.threekt.core.Geometry
import info.laht.threekt.objects.Mesh

/**
 * This should be inner, becaulse it uses object builder
 */
class ThreeCompositeFactory(val three: ThreePlugin) : MeshThreeFactory<Composite>(Composite::class) {

    override fun buildGeometry(obj: Composite): BufferGeometry {
        val first = three.buildObject3D(obj.first) as? Mesh ?: error("First part of composite is not a mesh")
        first.updateMatrix()
        val second = three.buildObject3D(obj.second) as? Mesh ?: error("Second part of composite is not a mesh")
        second.updateMatrix()
        val firstCSG = CSG.fromMesh(first)
        val secondCSG = CSG.fromMesh(second)
        val resultCSG = when (obj.type) {
            CompositeType.UNION -> firstCSG.union(secondCSG)
            CompositeType.INTERSECT -> firstCSG.intersect(secondCSG)
            CompositeType.SUBTRACT -> firstCSG.subtract(secondCSG)
        }

        val mesh = CSG.toMesh(resultCSG, second.matrix)
        return (mesh.geometry as Geometry).toBufferGeometry()
    }

}