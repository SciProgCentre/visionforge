package hep.dataforge.vision.solid.three

import hep.dataforge.meta.boolean
import hep.dataforge.meta.node
import hep.dataforge.names.Name
import hep.dataforge.names.asName
import hep.dataforge.names.plus
import hep.dataforge.names.startsWith
import hep.dataforge.vision.solid.Solid
import hep.dataforge.vision.solid.SolidMaterial
import hep.dataforge.vision.solid.layer
import hep.dataforge.vision.solid.three.ThreeMaterials.getMaterial
import info.laht.threekt.core.BufferGeometry
import info.laht.threekt.geometries.EdgesGeometry
import info.laht.threekt.geometries.WireframeGeometry
import info.laht.threekt.objects.LineSegments
import info.laht.threekt.objects.Mesh
import kotlin.reflect.KClass

/**
 * Basic geometry-based factory
 */
public abstract class MeshThreeFactory<in T : Solid>(
    override val type: KClass<in T>,
) : ThreeFactory<T> {
    /**
     * Build a geometry for an object
     */
    public abstract fun buildGeometry(obj: T): BufferGeometry

    override fun invoke(three: ThreePlugin, obj: T): Mesh {
        val geometry = buildGeometry(obj)

        //JS sometimes tries to pass Geometry as BufferGeometry
        @Suppress("USELESS_IS_CHECK") if (geometry !is BufferGeometry) error("BufferGeometry expected")

        //val meshMeta: Meta = obj.properties[Material3D.MATERIAL_KEY]?.node ?: Meta.empty

        val mesh = Mesh(geometry, null).apply {
            matrixAutoUpdate = false
            //set position for mesh
            updatePosition(obj)
        }.applyProperties(obj)

        //add listener to object properties
        obj.onPropertyChange(three.updateScope) { name ->
            when {
                name.startsWith(Solid.GEOMETRY_KEY) -> {
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

    public companion object {
        public val EDGES_KEY: Name = "edges".asName()
        public val WIREFRAME_KEY: Name = "wireframe".asName()
        public val ENABLED_KEY: Name = "enabled".asName()
        public val EDGES_ENABLED_KEY: Name = EDGES_KEY + ENABLED_KEY
        public val EDGES_MATERIAL_KEY: Name = EDGES_KEY + SolidMaterial.MATERIAL_KEY
        public val WIREFRAME_ENABLED_KEY: Name = WIREFRAME_KEY + ENABLED_KEY
        public val WIREFRAME_MATERIAL_KEY: Name = WIREFRAME_KEY + SolidMaterial.MATERIAL_KEY
    }
}

internal fun Mesh.applyProperties(obj: Solid): Mesh = apply {
    material = getMaterial(obj, true)
    applyEdges(obj)
    applyWireFrame(obj)
    layers.enable(obj.layer)
    children.forEach {
        it.layers.enable(obj.layer)
    }
}

public fun Mesh.applyEdges(obj: Solid) {
    val edges = children.find { it.name == "@edges" } as? LineSegments
    //inherited edges definition, enabled by default
    if (obj.getProperty(MeshThreeFactory.EDGES_ENABLED_KEY).boolean != false) {
        val bufferGeometry = geometry as? BufferGeometry ?: return
        val material = ThreeMaterials.getLineMaterial(obj.getProperty(MeshThreeFactory.EDGES_MATERIAL_KEY).node, true)
        if (edges == null) {
            add(
                LineSegments(
                    EdgesGeometry(bufferGeometry),
                    material
                ).apply {
                    name = "@edges"
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

public fun Mesh.applyWireFrame(obj: Solid) {
    children.find { it.name == "@wireframe" }?.let {
        remove(it)
        (it as LineSegments).dispose()
    }
    //inherited wireframe definition, disabled by default
    if (obj.getProperty(MeshThreeFactory.WIREFRAME_ENABLED_KEY).boolean == true) {
        val bufferGeometry = geometry as? BufferGeometry ?: return
        val material =
            ThreeMaterials.getLineMaterial(obj.getProperty(MeshThreeFactory.WIREFRAME_MATERIAL_KEY).node, true)
        add(
            LineSegments(
                WireframeGeometry(bufferGeometry),
                material
            ).apply {
                name = "@wireframe"
            }
        )
    }
}