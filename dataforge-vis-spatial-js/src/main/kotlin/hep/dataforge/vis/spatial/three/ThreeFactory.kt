package hep.dataforge.vis.spatial.three

import hep.dataforge.meta.boolean
import hep.dataforge.names.startsWith
import hep.dataforge.names.toName
import hep.dataforge.provider.Type
import hep.dataforge.vis.common.VisualObject
import hep.dataforge.vis.common.getProperty
import hep.dataforge.vis.common.onChange
import hep.dataforge.vis.spatial.*
import hep.dataforge.vis.spatial.three.ThreeFactory.Companion.TYPE
import hep.dataforge.vis.spatial.three.ThreeFactory.Companion.buildMesh
import info.laht.threekt.core.BufferGeometry
import info.laht.threekt.core.Object3D
import info.laht.threekt.external.geometries.ConvexBufferGeometry
import info.laht.threekt.geometries.EdgesGeometry
import info.laht.threekt.geometries.WireframeGeometry
import info.laht.threekt.objects.LineSegments
import info.laht.threekt.objects.Mesh
import kotlin.reflect.KClass

internal val VisualObject.material get() = getProperty("material").material()

/**
 * Builder and updater for three.js object
 */
@Type(TYPE)
interface ThreeFactory<T : VisualObject> {

    val type: KClass<out T>

    operator fun invoke(obj: T): Object3D

    companion object {
        const val TYPE = "threeFactory"

        fun <T : VisualObject> buildMesh(obj: T, geometryBuilder: (T) -> BufferGeometry): Mesh {
            val geometry = geometryBuilder(obj)

            //JS sometimes tries to pass Geometry as BufferGeometry
            @Suppress("USELESS_IS_CHECK") if (geometry !is BufferGeometry) error("BufferGeometry expected")

            val mesh = Mesh(geometry, obj.material)

            //inherited edges definition, enabled by default
            if (obj.getProperty("edges.enabled").boolean != false) {
                val material = obj.getProperty("edges.material")?.material() ?: Materials.DEFAULT
                mesh.add(LineSegments(EdgesGeometry(mesh.geometry as BufferGeometry), material))
            }

            //inherited wireframe definition, disabled by default
            if (obj.getProperty("wireframe.enabled").boolean == true) {
                val material = obj.getProperty("edges.material")?.material() ?: Materials.DEFAULT
                mesh.add(LineSegments(WireframeGeometry(mesh.geometry as BufferGeometry), material))
            }

            //set position for meseh
            mesh.updatePosition(obj)

            //add listener to object properties
            obj.onChange(this) { name, _, _ ->
                if (name.toString() == "material") {
                    //updated material
                    mesh.material = obj.material
                } else if (
                    name.startsWith("pos".toName()) ||
                    name.startsWith("scale".toName()) ||
                    name.startsWith("rotation".toName()) ||
                    name.toString() == "visible"
                ) {
                    //update position of mesh using this object
                    mesh.updatePosition(obj)
                } else {
                    //full update
                    mesh.geometry = geometryBuilder(obj)
                    mesh.material = obj.material
                }
            }
            return mesh
        }
    }
}

/**
 * Update position, rotation and visibility
 */
internal fun Object3D.updatePosition(obj: VisualObject) {
    position.set(obj.x, obj.y, obj.z)
    setRotationFromEuler(obj.euler)
    scale.set(obj.scaleX, obj.scaleY, obj.scaleZ)
    visible = obj.visible
}

/**
 * Unsafe invocation of a factory
 */
operator fun <T : VisualObject> ThreeFactory<T>.invoke(obj: Any): Object3D {
    if (type.isInstance(obj)) {
        return invoke(obj as T)
    } else {
        error("The object of type ${obj::class} could not be rendered by this factory")
    }
}

/**
 * Basic geometry-based factory
 */
abstract class MeshThreeFactory<T : VisualObject>(override val type: KClass<out T>) :
    ThreeFactory<T> {
    /**
     * Build a geometry for an object
     */
    abstract fun buildGeometry(obj: T): BufferGeometry


    override fun invoke(obj: T): Mesh {
        //create mesh from geometry
        val mesh = buildMesh(obj, ::buildGeometry)
        return mesh
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

//FIXME not functional yet
object ThreeConvexFactory : MeshThreeFactory<Convex>(Convex::class) {
    override fun buildGeometry(obj: Convex): ConvexBufferGeometry {
        val vectors = obj.points.map { it.asVector() }.toTypedArray()
        return ConvexBufferGeometry(vectors)
    }
}