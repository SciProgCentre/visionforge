package hep.dataforge.vis.spatial.three

import hep.dataforge.meta.boolean
import hep.dataforge.meta.int
import hep.dataforge.meta.node
import hep.dataforge.names.plus
import hep.dataforge.names.startsWith
import hep.dataforge.provider.Type
import hep.dataforge.vis.common.asName
import hep.dataforge.vis.spatial.*
import hep.dataforge.vis.spatial.three.ThreeFactory.Companion.TYPE
import info.laht.threekt.core.BufferGeometry
import info.laht.threekt.core.Object3D
import info.laht.threekt.geometries.EdgesGeometry
import info.laht.threekt.geometries.WireframeGeometry
import info.laht.threekt.objects.LineSegments
import info.laht.threekt.objects.Mesh
import kotlin.reflect.KClass

/**
 * Builder and updater for three.js object
 */
@Type(TYPE)
interface ThreeFactory<T : VisualObject3D> {

    val type: KClass<out T>

    operator fun invoke(obj: T): Object3D

    companion object {
        const val TYPE = "threeFactory"
    }
}

/**
 * Update position, rotation and visibility
 */
internal fun Object3D.updatePosition(obj: VisualObject3D) {
    visible = obj.visible ?: true
//    Matrix4().apply {
//        makeRotationFromEuler(obj.euler)
//        applyMatrix(this)
//        makeTranslation(obj.x.toDouble(), obj.y.toDouble(), obj.z.toDouble())
//        applyMatrix(this)
//    }
    position.set(obj.x, obj.y, obj.z)
    setRotationFromEuler(obj.euler)
    scale.set(obj.scaleX, obj.scaleY, obj.scaleZ)
    updateMatrix()
}

internal fun <T : VisualObject3D> Mesh.updateFrom(obj: T) {
    matrixAutoUpdate = false

    //inherited edges definition, enabled by default
    if (obj.getProperty(MeshThreeFactory.EDGES_ENABLED_KEY).boolean != false) {
        val material = obj.getProperty(MeshThreeFactory.EDGES_MATERIAL_KEY).node?.jsMaterial() ?: Materials.DEFAULT
        add(LineSegments(EdgesGeometry(geometry as BufferGeometry), material))
    }

    //inherited wireframe definition, disabled by default
    if (obj.getProperty(MeshThreeFactory.WIREFRAME_ENABLED_KEY).boolean == true) {
        val material = obj.getProperty(MeshThreeFactory.WIREFRAME_MATERIAL_KEY).node?.jsMaterial() ?: Materials.DEFAULT
        add(LineSegments(WireframeGeometry(geometry as BufferGeometry), material))
    }

    //set position for mesh
    updatePosition(obj)

    obj.getProperty(MeshThreeFactory.LAYER_KEY).int?.let {
        layers.set(it)
    }
}

/**
 * Unsafe invocation of a factory
 */
operator fun <T : VisualObject3D> ThreeFactory<T>.invoke(obj: Any): Object3D {
    if (type.isInstance(obj)) {
        @Suppress("UNCHECKED_CAST")
        return invoke(obj as T)
    } else {
        error("The object of type ${obj::class} could not be rendered by this factory")
    }
}

/**
 * Basic geometry-based factory
 */
abstract class MeshThreeFactory<T : VisualObject3D>(override val type: KClass<out T>) : ThreeFactory<T> {
    /**
     * Build a geometry for an object
     */
    abstract fun buildGeometry(obj: T): BufferGeometry


    override fun invoke(obj: T): Mesh {
        //create mesh from geometry
        return buildMesh<T>(obj) { buildGeometry(it) }
    }

    companion object {
        val EDGES_KEY = "edges".asName()
        val WIREFRAME_KEY = "wireframe".asName()
        val ENABLED_KEY = "enabled".asName()
        val EDGES_ENABLED_KEY = EDGES_KEY + ENABLED_KEY
        val EDGES_MATERIAL_KEY = EDGES_KEY + VisualObject3D.MATERIAL_KEY
        val WIREFRAME_ENABLED_KEY = WIREFRAME_KEY + ENABLED_KEY
        val WIREFRAME_MATERIAL_KEY = WIREFRAME_KEY + VisualObject3D.MATERIAL_KEY
        val LAYER_KEY = "layer".asName()

        fun <T : VisualObject3D> buildMesh(obj: T, geometryBuilder: (T) -> BufferGeometry): Mesh {
            //TODO add caching for geometries using templates
            val geometry = geometryBuilder(obj)

            //JS sometimes tries to pass Geometry as BufferGeometry
            @Suppress("USELESS_IS_CHECK") if (geometry !is BufferGeometry) error("BufferGeometry expected")

            val mesh = Mesh(geometry, obj.material.jsMaterial())

            mesh.updateFrom(obj)

            //add listener to object properties
            obj.onPropertyChange(this) { name, _, _ ->
                if (name.startsWith(VisualObject3D.MATERIAL_KEY)) {
                    //updated material
                    mesh.material = obj.material.jsMaterial()
                } else if (
                    name.startsWith(VisualObject3D.position) ||
                    name.startsWith(VisualObject3D.rotation) ||
                    name.startsWith(VisualObject3D.scale)
                ) {
                    //update position of mesh using this object
                    mesh.updatePosition(obj)
                } else if (name == VisualObject3D.VISIBLE_KEY) {
                    mesh.visible = obj.visible ?: true
                } else {
                    //full update
                    mesh.geometry = geometryBuilder(obj)
                    mesh.material = obj.material.jsMaterial()
                }
            }
            return mesh
        }
    }
}

/**
 * Generic factory for elements which provide inside geometry builder
 */
object ThreeShapeFactory : MeshThreeFactory<Shape>(Shape::class) {
    override fun buildGeometry(obj: Shape): BufferGeometry {
        return obj.run {
            ThreeGeometryBuilder().apply { toGeometry(this) }.build()
        }
    }
}