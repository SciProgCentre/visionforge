package hep.dataforge.vis.spatial

import hep.dataforge.meta.boolean
import hep.dataforge.provider.Type
import hep.dataforge.vis.common.DisplayObject
import hep.dataforge.vis.common.getProperty
import hep.dataforge.vis.common.onChange
import hep.dataforge.vis.spatial.ThreeFactory.Companion.TYPE
import hep.dataforge.vis.spatial.ThreeFactory.Companion.buildMesh
import hep.dataforge.vis.spatial.ThreeFactory.Companion.updateMesh
import hep.dataforge.vis.spatial.three.ConvexBufferGeometry
import hep.dataforge.vis.spatial.three.EdgesGeometry
import hep.dataforge.vis.spatial.three.euler
import info.laht.threekt.core.BufferGeometry
import info.laht.threekt.core.Object3D
import info.laht.threekt.geometries.BoxBufferGeometry
import info.laht.threekt.geometries.WireframeGeometry
import info.laht.threekt.math.Vector3
import info.laht.threekt.objects.LineSegments
import info.laht.threekt.objects.Mesh
import kotlin.reflect.KClass

internal val DisplayObject.material get() = getProperty("color").material()

/**
 * Builder and updater for three.js object
 */
@Type(TYPE)
interface ThreeFactory<T : DisplayObject> {

    val type: KClass<out T>

    operator fun invoke(obj: T): Object3D

    companion object {
        const val TYPE = "threeFactory"

        /**
         * Update position, rotation and visibility
         */
        internal fun updatePosition(obj: DisplayObject, target: Object3D) {
            target.apply {
                position.set(obj.x, obj.y, obj.z)
                setRotationFromEuler(obj.euler)
                scale.set(obj.scaleX, obj.scaleY, obj.scaleZ)
                visible = obj.visible
            }
        }

        internal fun buildMesh(obj: DisplayObject, geometry: BufferGeometry): Mesh {
            val mesh = Mesh(geometry, obj.material)
            if (obj.getProperty("edges.enabled")?.boolean != false) {
                val material = obj.getProperty("edges.material")?.material() ?: Materials.DEFAULT
                mesh.add(LineSegments(EdgesGeometry(mesh.geometry as BufferGeometry), material))
            }

            if (obj.getProperty("wireframe.enabled")?.boolean == true) {
                val material = obj.getProperty("edges.material")?.material() ?: Materials.DEFAULT
                mesh.add(LineSegments(WireframeGeometry(mesh.geometry as BufferGeometry), material))
            }
            return mesh
        }

        internal fun updateMesh(obj: DisplayObject, geometry: BufferGeometry, mesh: Mesh) {
            mesh.geometry = geometry
            mesh.material = obj.material
        }
    }
}

operator fun <T : DisplayObject> ThreeFactory<T>.invoke(obj: Any): Object3D {
    if (type.isInstance(obj)) {
        return invoke(obj as T)
    } else {
        error("The object of type ${obj::class} could not be rendered by this factory")
    }
}

abstract class MeshThreeFactory<T : DisplayObject>(override val type: KClass<out T>) : ThreeFactory<T> {
    /**
     * Build an object
     */
    abstract fun buildGeometry(obj: T): BufferGeometry


    override fun invoke(obj: T): Mesh {
        val geometry = buildGeometry(obj)

        @Suppress("USELESS_IS_CHECK") if (geometry !is BufferGeometry) error("BufferGeometry expected")

        val mesh = buildMesh(obj, geometry)
        ThreeFactory.updatePosition(obj, mesh)
        obj.onChange(this) { _, _, _ ->
            ThreeFactory.updatePosition(obj, mesh)
            updateMesh(obj, buildGeometry(obj), mesh)
        }
        return mesh
    }
}

/**
 * Generic factory for elements which provide inside geometry builder
 */
object ThreeShapeFactory : MeshThreeFactory<Shape>(Shape::class) {
    override fun buildGeometry(obj: Shape): BufferGeometry {
        return obj.run {
            ThreeGeometryBuilder().apply { buildGeometry() }.build()
        }
    }
}

object ThreeBoxFactory : MeshThreeFactory<Box>(Box::class) {
    override fun buildGeometry(obj: Box) =
        BoxBufferGeometry(obj.xSize, obj.ySize, obj.zSize)
}

fun Point3D.asVector(): Vector3 = Vector3(this.x, this.y, this.z)

object ThreeConvexFactory : MeshThreeFactory<Convex>(Convex::class) {
    override fun buildGeometry(obj: Convex): ConvexBufferGeometry {
        val vectors = obj.points.map { it.asVector() }.toTypedArray()
        return ConvexBufferGeometry(vectors)
    }
}