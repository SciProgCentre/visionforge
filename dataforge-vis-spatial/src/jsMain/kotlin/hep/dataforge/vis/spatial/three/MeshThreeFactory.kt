package hep.dataforge.vis.spatial.three

import hep.dataforge.meta.boolean
import hep.dataforge.meta.node
import hep.dataforge.names.asName
import hep.dataforge.names.plus
import hep.dataforge.names.startsWith
import hep.dataforge.vis.spatial.Material3D
import hep.dataforge.vis.spatial.VisualObject3D
import hep.dataforge.vis.spatial.layer
import hep.dataforge.vis.spatial.three.ThreeMaterials.getMaterial
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

    override fun invoke(obj: T): Mesh {
        val geometry = buildGeometry(obj)

        //JS sometimes tries to pass Geometry as BufferGeometry
        @Suppress("USELESS_IS_CHECK") if (geometry !is BufferGeometry) error("BufferGeometry expected")

        //val meshMeta: Meta = obj.properties[Material3D.MATERIAL_KEY]?.node ?: Meta.empty

        val mesh = Mesh(geometry, getMaterial(obj)).apply {
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
                name.startsWith(VisualObject3D.GEOMETRY_KEY) -> {
                    val oldGeometry = mesh.geometry as BufferGeometry
                    val newGeometry = buildGeometry(obj)
                    oldGeometry.attributes = newGeometry.attributes
                    mesh.applyWireFrame(obj)
                    mesh.applyEdges(obj)
                    newGeometry.dispose()
                }
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

fun Mesh.applyEdges(obj: VisualObject3D) {
    children.find { it.name == "@edges" }?.let {
        remove(it)
        (it as LineSegments).dispose()
    }
    //inherited edges definition, enabled by default
    if (obj.getItem(MeshThreeFactory.EDGES_ENABLED_KEY).boolean != false) {

        val material = ThreeMaterials.getLineMaterial(obj.getItem(MeshThreeFactory.EDGES_MATERIAL_KEY).node)
        add(
            LineSegments(
                EdgesGeometry(geometry as BufferGeometry),
                material
            ).apply {
                name = "@edges"
            }
        )
    }
}

fun Mesh.applyWireFrame(obj: VisualObject3D) {
    children.find { it.name == "@wireframe" }?.let {
        remove(it)
        (it as LineSegments).dispose()
    }
    //inherited wireframe definition, disabled by default
    if (obj.getItem(MeshThreeFactory.WIREFRAME_ENABLED_KEY).boolean == true) {
        val material = ThreeMaterials.getLineMaterial(obj.getItem(MeshThreeFactory.WIREFRAME_MATERIAL_KEY).node)
        add(
            LineSegments(
                WireframeGeometry(geometry as BufferGeometry),
                material
            ).apply {
                name = "@wireframe"
            }
        )
    }
}