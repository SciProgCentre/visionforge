package space.kscience.visionforge.solid.three

import space.kscience.dataforge.meta.boolean
import space.kscience.dataforge.names.startsWith
import space.kscience.visionforge.onPropertyChange
import space.kscience.visionforge.solid.Solid
import space.kscience.visionforge.solid.SolidMaterial.Companion.EDGES_ENABLED_KEY
import space.kscience.visionforge.solid.SolidMaterial.Companion.EDGES_KEY
import space.kscience.visionforge.solid.SolidMaterial.Companion.EDGES_MATERIAL_KEY
import space.kscience.visionforge.solid.layer
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
    public abstract suspend fun buildGeometry(obj: T): BufferGeometry

    override suspend fun build(three: ThreePlugin, vision: T, observe: Boolean): Mesh {
        val geometry = buildGeometry(vision)

        val mesh = Mesh(geometry, ThreeMaterials.DEFAULT).apply {
            matrixAutoUpdate = false
            //set position for mesh
            updatePosition(vision)
            applyProperties(vision)
        }

        if (observe) {
            //add listener to object properties
            vision.onPropertyChange(three.context) { name ->
                when {
                    name.startsWith(Solid.GEOMETRY_KEY) -> {
                        val oldGeometry = mesh.geometry
                        val newGeometry = buildGeometry(vision)
                        oldGeometry.attributes = newGeometry.attributes

                        mesh.applyEdges(vision)
                        newGeometry.dispose()
                    }
                    name.startsWith(EDGES_KEY) -> mesh.applyEdges(vision)
                    else -> mesh.updateProperty(vision, name)
                }
            }
        }

        return mesh
    }

    public companion object {
        internal const val EDGES_OBJECT_NAME: String = "@edges"
    }
}

internal fun Mesh.applyProperties(vision: Solid): Mesh = apply {
    setMaterial(vision)
    applyEdges(vision)
    layers.set(vision.layer)
    children.forEach {
        it.layers.set(vision.layer)
    }
}

public fun Mesh.applyEdges(vision: Solid) {
    val edges = children.find { it.name == EDGES_OBJECT_NAME } as? LineSegments
    //inherited edges definition, enabled by default
    if (vision.properties.getValue(EDGES_ENABLED_KEY, inherit = false)?.boolean != false) {
        val material = ThreeMaterials.getLineMaterial(vision.properties.getProperty(EDGES_MATERIAL_KEY), true)
        if (edges == null) {
            add(
                LineSegments(
                    EdgesGeometry(geometry),
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
