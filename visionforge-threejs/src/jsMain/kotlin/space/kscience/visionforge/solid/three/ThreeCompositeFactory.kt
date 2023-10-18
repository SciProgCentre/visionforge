package space.kscience.visionforge.solid.three

import CSG
import space.kscience.dataforge.names.startsWith
import space.kscience.visionforge.onPropertyChange
import space.kscience.visionforge.solid.Composite
import space.kscience.visionforge.solid.CompositeType
import space.kscience.visionforge.solid.SolidMaterial.Companion.EDGES_KEY
import three.objects.Mesh
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

    override suspend fun build(three: ThreePlugin, vision: Composite, observe: Boolean): Mesh {
        val first = three.buildObject3D(vision.first, observe).takeIfMesh()
            ?: error("First part of composite is not a mesh")
        val second = three.buildObject3D(vision.second, observe).takeIfMesh()
            ?: error("Second part of composite is not a mesh")
        return when (vision.compositeType) {
            CompositeType.GROUP, CompositeType.UNION -> CSG.union(first, second)
            CompositeType.INTERSECT -> CSG.intersect(first, second)
            CompositeType.SUBTRACT -> CSG.subtract(first, second)
        }.apply {
            updatePosition(vision)
            applyProperties(vision)

            if (observe) {
                vision.onPropertyChange { name ->
                    when {
                        //name.startsWith(WIREFRAME_KEY) -> mesh.applyWireFrame(obj)
                        name.startsWith(EDGES_KEY) -> applyEdges(vision)
                        else -> updateProperty(vision, name)
                    }
                }
            }
        }
    }
}