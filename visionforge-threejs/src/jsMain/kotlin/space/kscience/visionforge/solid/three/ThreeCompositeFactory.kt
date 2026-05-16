package space.kscience.visionforge.solid.three

import CSG
import space.kscience.dataforge.meta.boolean
import space.kscience.dataforge.meta.get
import space.kscience.dataforge.names.startsWith
import space.kscience.visionforge.onPropertyChange
import space.kscience.visionforge.solid.Composite
import space.kscience.visionforge.solid.CompositeType
import space.kscience.visionforge.solid.GeometrySolid
import space.kscience.visionforge.solid.SolidMaterial.Companion.EDGES_KEY
import space.kscience.visionforge.solid.composite
import three.objects.Mesh
import kotlin.reflect.KClass

/**
 * This should be inner, because it uses an object builder
 */
public object ThreeCompositeFactory : ThreeFactory<Composite> {

    override val type: KClass<in Composite> get() = Composite::class

    override suspend fun build(three: ThreePlugin, vision: Composite, observe: Boolean): Mesh {

        val mesh: Mesh = if (
            (vision.properties["forceThreeCsg"].boolean != true) &&
            vision.first is GeometrySolid && vision.second is GeometrySolid
        ) {
            val bufferGeometry = ThreeGeometryBuilder().apply {
                composite(vision)
            }.build()

            Mesh(bufferGeometry, ThreeMaterials.DEFAULT)
        } else {

            val first = three.buildObject3D(vision.first, observe).takeIfMesh()
                ?: error("First part of composite is not a mesh")
            val second = three.buildObject3D(vision.second, observe).takeIfMesh()
                ?: error("Second part of composite is not a mesh")

            when (vision.compositeType) {
                CompositeType.GROUP, CompositeType.UNION -> CSG.union(first, second)
                CompositeType.INTERSECT -> CSG.intersect(first, second)
                CompositeType.SUBTRACT -> CSG.subtract(first, second)
            }
        }

        return mesh.apply {
            updatePosition(vision)
            applyProperties(vision)

            if (observe) {
                vision.onPropertyChange(three.context) { name, _ ->
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