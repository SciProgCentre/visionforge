package space.kscience.visionforge.solid.three

import space.kscience.dataforge.meta.boolean
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.asName
import space.kscience.dataforge.names.plus
import space.kscience.dataforge.names.startsWith
import space.kscience.visionforge.VisionBuilder
import space.kscience.visionforge.onPropertyChange
import space.kscience.visionforge.set
import space.kscience.visionforge.solid.Solid
import space.kscience.visionforge.solid.SolidMaterial
import space.kscience.visionforge.solid.layer
import space.kscience.visionforge.solid.three.ThreeMeshFactory.Companion.EDGES_ENABLED_KEY
import space.kscience.visionforge.solid.three.ThreeMeshFactory.Companion.EDGES_MATERIAL_KEY
import space.kscience.visionforge.solid.three.ThreeMeshFactory.Companion.EDGES_OBJECT_NAME
import three.core.BufferGeometry
import three.geometries.EdgesGeometry
import three.objects.LineSegments
import three.objects.Mesh
import kotlin.reflect.KClass

/**
 * Basic geometry-based factory
 */
public abstract class ThreeMeshFactory<in T : Solid>(
    override val type: KClass<in T>,
) : ThreeFactory<T> {
    /**
     * Build a geometry for an object
     */
    public abstract fun buildGeometry(obj: T): BufferGeometry

    override fun build(three: ThreePlugin, vision: T, observe: Boolean): Mesh {
        val geometry = buildGeometry(vision)

        //val meshMeta: Meta = obj.properties[Material3D.MATERIAL_KEY]?.node ?: Meta.empty

        val mesh = Mesh(geometry, ThreeMaterials.DEFAULT).apply {
            matrixAutoUpdate = false
            //set position for mesh
            updatePosition(vision)
            applyProperties(vision)
        }

        if(observe) {
            //add listener to object properties
            vision.onPropertyChange(three.context) { name ->
                when {
                    name.startsWith(Solid.GEOMETRY_KEY) -> {
                        val oldGeometry = mesh.geometry
                        val newGeometry = buildGeometry(vision)
                        oldGeometry.attributes = newGeometry.attributes
                        //mesh.applyWireFrame(obj)
                        mesh.applyEdges(vision)
                        newGeometry.dispose()
                    }
                    //name.startsWith(WIREFRAME_KEY) -> mesh.applyWireFrame(obj)
                    name.startsWith(EDGES_KEY) -> mesh.applyEdges(vision)
                    else -> mesh.updateProperty(vision, name)
                }
            }
        }

        return mesh
    }

    public companion object {
        public val EDGES_KEY: Name = "edges".asName()
        internal const val EDGES_OBJECT_NAME: String = "@edges"

        //public val WIREFRAME_KEY: Name = "wireframe".asName()
        public val ENABLED_KEY: Name = "enabled".asName()
        public val EDGES_ENABLED_KEY: Name = EDGES_KEY + ENABLED_KEY
        public val EDGES_MATERIAL_KEY: Name = EDGES_KEY + SolidMaterial.MATERIAL_KEY
        //public val WIREFRAME_ENABLED_KEY: Name = WIREFRAME_KEY + ENABLED_KEY
        //public val WIREFRAME_MATERIAL_KEY: Name = WIREFRAME_KEY + SolidMaterial.MATERIAL_KEY
    }
}

@VisionBuilder
public fun Solid.edges(enabled: Boolean = true, block: SolidMaterial.() -> Unit = {}) {
    properties[EDGES_ENABLED_KEY] = enabled
    SolidMaterial.write(properties.getProperty(EDGES_MATERIAL_KEY)).apply(block)
}

internal fun Mesh.applyProperties(vision: Solid): Mesh = apply {
    createMaterial(vision)
    applyEdges(vision)
    //applyWireFrame(obj)
    layers.set(vision.layer)
    children.forEach {
        it.layers.set(vision.layer)
    }
}

public fun Mesh.applyEdges(vision: Solid) {
    val edges = children.find { it.name == EDGES_OBJECT_NAME } as? LineSegments
    //inherited edges definition, enabled by default
    if (vision.properties.getValue(EDGES_ENABLED_KEY, inherit = true)?.boolean != false) {
        val bufferGeometry = geometry as? BufferGeometry ?: return
        val material = ThreeMaterials.getLineMaterial(vision.properties.getProperty(EDGES_MATERIAL_KEY), true)
        if (edges == null) {
            add(
                LineSegments(
                    EdgesGeometry(bufferGeometry),
                    material
                ).apply {
                    name = EDGES_OBJECT_NAME
                }
            )
        } else {
            edges.material = material
        }
    } else {
        edges?.let {
            remove(it)
            it.dispose()
        }
    }
}

//public fun Mesh.applyWireFrame(obj: Solid) {
//    children.find { it.name == "@wireframe" }?.let {
//        remove(it)
//        (it as LineSegments).dispose()
//    }
//    //inherited wireframe definition, disabled by default
//    if (obj.getProperty(MeshThreeFactory.WIREFRAME_ENABLED_KEY).boolean == true) {
//        val bufferGeometry = geometry as? BufferGeometry ?: return
//        val material =
//            ThreeMaterials.getLineMaterial(obj.getProperty(MeshThreeFactory.WIREFRAME_MATERIAL_KEY).node, true)
//        add(
//            LineSegments(
//                WireframeGeometry(bufferGeometry),
//                material
//            ).apply {
//                name = "@wireframe"
//            }
//        )
//    }
//}