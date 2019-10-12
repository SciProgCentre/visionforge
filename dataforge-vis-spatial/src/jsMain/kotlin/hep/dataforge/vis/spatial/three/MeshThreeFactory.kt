package hep.dataforge.vis.spatial.three

import hep.dataforge.meta.boolean
import hep.dataforge.meta.node
import hep.dataforge.names.asName
import hep.dataforge.names.plus
import hep.dataforge.names.startsWith
import hep.dataforge.vis.spatial.Material3D
import hep.dataforge.vis.spatial.VisualObject3D
import hep.dataforge.vis.spatial.layer
import hep.dataforge.vis.spatial.material
import info.laht.threekt.core.BufferGeometry
import info.laht.threekt.geometries.EdgesGeometry
import info.laht.threekt.geometries.WireframeGeometry
import info.laht.threekt.objects.LineSegments
import info.laht.threekt.objects.Mesh
import kotlin.reflect.KClass

/**
 * Basic geometry-based factory
 */
abstract class MeshThreeFactory<in T : VisualObject3D>(
    override val type: KClass<in T>
) : ThreeFactory<T> {
    /**
     * Build a geometry for an object
     */
    abstract fun buildGeometry(obj: T): BufferGeometry

    private fun Mesh.applyEdges(obj: T) {
        children.find { it.name == "edges" }?.let { remove(it) }
        //inherited edges definition, enabled by default
        if (obj.getProperty(EDGES_ENABLED_KEY).boolean != false) {
            val material = obj.getProperty(EDGES_MATERIAL_KEY).node.jsLineMaterial()
            add(
                LineSegments(
                    EdgesGeometry(geometry as BufferGeometry),
                    material
                )
            )
        }
    }

    private fun Mesh.applyWireFrame(obj: T) {
        children.find { it.name == "wireframe" }?.let { remove(it) }
        //inherited wireframe definition, disabled by default
        if (obj.getProperty(WIREFRAME_ENABLED_KEY).boolean == true) {
            val material = obj.getProperty(WIREFRAME_MATERIAL_KEY).node.jsLineMaterial()
            add(
                LineSegments(
                    WireframeGeometry(geometry as BufferGeometry),
                    material
                )
            )
        }
    }

    override fun invoke(obj: T): Mesh {
        //TODO add caching for geometries using templates
        val geometry = buildGeometry(obj)

        //JS sometimes tries to pass Geometry as BufferGeometry
        @Suppress("USELESS_IS_CHECK") if (geometry !is BufferGeometry) error("BufferGeometry expected")

        val mesh = Mesh(geometry, obj.material.jsMaterial()).apply {
            matrixAutoUpdate = false
            applyEdges(obj)
            applyWireFrame(obj)
            //set position for mesh
            updatePosition(obj)

            layers.enable(obj.layer)
            children.forEach {
                it.layers.enable(obj.layer)
            }
        }

        //add listener to object properties
        obj.onPropertyChange(this) { name, _, _ ->
            when {
                name.startsWith(VisualObject3D.GEOMETRY_KEY) -> mesh.geometry = buildGeometry(obj)
                name.startsWith(WIREFRAME_KEY) -> mesh.applyWireFrame(obj)
                name.startsWith(EDGES_KEY) -> mesh.applyEdges(obj)
                else -> mesh.updateProperty(obj, name)
            }
        }
        return mesh
    }

    companion object {
        val EDGES_KEY = "edges".asName()
        val WIREFRAME_KEY = "wireframe".asName()
        val ENABLED_KEY = "enabled".asName()
        val EDGES_ENABLED_KEY = EDGES_KEY + ENABLED_KEY
        val EDGES_MATERIAL_KEY = EDGES_KEY + Material3D.MATERIAL_KEY
        val WIREFRAME_ENABLED_KEY = WIREFRAME_KEY + ENABLED_KEY
        val WIREFRAME_MATERIAL_KEY = WIREFRAME_KEY + Material3D.MATERIAL_KEY
    }
}