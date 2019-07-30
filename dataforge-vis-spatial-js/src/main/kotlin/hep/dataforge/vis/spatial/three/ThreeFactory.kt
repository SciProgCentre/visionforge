package hep.dataforge.vis.spatial.three

import hep.dataforge.meta.boolean
import hep.dataforge.meta.get
import hep.dataforge.meta.int
import hep.dataforge.meta.node
import hep.dataforge.names.startsWith
import hep.dataforge.provider.Type
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

/**
 * Builder and updater for three.js object
 */
@Type(TYPE)
interface ThreeFactory<T : VisualObject3D> {

    val type: KClass<out T>

    operator fun invoke(obj: T): Object3D

    companion object {
        const val TYPE = "threeFactory"

        fun <T : VisualObject3D> buildMesh(obj: T, geometryBuilder: (T) -> BufferGeometry): Mesh {
            val geometry = geometryBuilder(obj)

            //JS sometimes tries to pass Geometry as BufferGeometry
            @Suppress("USELESS_IS_CHECK") if (geometry !is BufferGeometry) error("BufferGeometry expected")

            val mesh = Mesh(geometry, obj.material.jsMaterial())

            //inherited edges definition, enabled by default
            if (obj.properties["edges.enabled"].boolean != false) {
                val material = obj.properties["edges.material"].node?.jsMaterial() ?: Materials.DEFAULT
                mesh.add(LineSegments(EdgesGeometry(mesh.geometry as BufferGeometry), material))
            }

            //inherited wireframe definition, disabled by default
            if (obj.properties["wireframe.enabled"].boolean == true) {
                val material = obj.properties["wireframe.material"].node?.jsMaterial() ?: Materials.DEFAULT
                mesh.add(LineSegments(WireframeGeometry(mesh.geometry as BufferGeometry), material))
            }

            //set position for mesh
            mesh.updatePosition(obj)

            obj.config["layer"].int?.let {
                mesh.layers.set(it)
            }

            //add listener to object properties
            obj.onChange(this) { name, _, _ ->
                if (name.startsWith(VisualObject3D.materialKey)) {
                    //updated material
                    mesh.material = obj.material.jsMaterial()
                } else if (
                    name.startsWith(VisualObject3D.position) ||
                    name.startsWith(VisualObject3D.rotation) ||
                    name.startsWith(VisualObject3D.scale) ||
                    name == VisualObject3D.visibleKey
                ) {
                    //update position of mesh using this object
                    mesh.updatePosition(obj)
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
 * Update position, rotation and visibility
 */
internal fun Object3D.updatePosition(obj: VisualObject3D) {
    position.set(obj.x, obj.y, obj.z)
    setRotationFromEuler(obj.euler)
    scale.set(obj.scaleX, obj.scaleY, obj.scaleZ)
    visible = obj.visible ?: true
}

/**
 * Unsafe invocation of a factory
 */
operator fun <T : VisualObject3D> ThreeFactory<T>.invoke(obj: Any): Object3D {
    if (type.isInstance(obj)) {
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