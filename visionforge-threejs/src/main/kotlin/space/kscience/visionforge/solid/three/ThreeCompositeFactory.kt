package space.kscience.visionforge.solid.three

import CSG
import info.laht.threekt.core.Object3D
import info.laht.threekt.objects.Mesh
import space.kscience.dataforge.names.startsWith
import space.kscience.visionforge.onPropertyChange
import space.kscience.visionforge.solid.Composite
import space.kscience.visionforge.solid.CompositeType
import kotlin.reflect.KClass

/**
 * This should be inner, because it uses object builder
 */
public class ThreeCompositeFactory(public val three: ThreePlugin) : ThreeFactory<Composite> {

//    override fun buildGeometry(obj: Composite): BufferGeometry {
//        val first = three.buildObject3D(obj.first) as? Mesh ?: error("First part of composite is not a mesh")
//        //first.updateMatrix()
//        val second = three.buildObject3D(obj.second) as? Mesh ?: error("Second part of composite is not a mesh")
//        //second.updateMatrix()
//        val firstCSG = CSG.fromMesh(first)
//        val secondCSG = CSG.fromMesh(second)
////        val resultCSG = when (obj.compositeType) {
////            CompositeType.UNION -> firstCSG.union(secondCSG)
////            CompositeType.INTERSECT -> firstCSG.intersect(secondCSG)
////            CompositeType.SUBTRACT -> firstCSG.subtract(secondCSG)
////        }
////        return resultCSG.toGeometry(second.matrix)
//
//        val resultMesh: Mesh = when (obj.compositeType) {
//            CompositeType.UNION -> CSG.union(first,second)
//            CompositeType.INTERSECT -> CSG.intersect(first,second)
//            CompositeType.SUBTRACT -> CSG.subtract(first,second)
//        }
//        return resultMesh.geometry
//    }

    override val type: KClass<in Composite> get() = Composite::class

    override fun invoke(three: ThreePlugin, obj: Composite): Object3D {
        val first = three.buildObject3D(obj.first) as? Mesh ?: error("First part of composite is not a mesh")
        val second = three.buildObject3D(obj.second) as? Mesh ?: error("Second part of composite is not a mesh")
        return when (obj.compositeType) {
            CompositeType.UNION -> CSG.union(first,second)
            CompositeType.INTERSECT -> CSG.intersect(first,second)
            CompositeType.SUBTRACT -> CSG.subtract(first,second)
        }.apply {
            updatePosition(obj)
            applyProperties(obj)
            obj.onPropertyChange(three.updateScope) { name ->
                when {
                    //name.startsWith(WIREFRAME_KEY) -> mesh.applyWireFrame(obj)
                    name.startsWith(MeshThreeFactory.EDGES_KEY) -> applyEdges(obj)
                    else -> updateProperty(obj, name)
                }
            }
        }
    }
}