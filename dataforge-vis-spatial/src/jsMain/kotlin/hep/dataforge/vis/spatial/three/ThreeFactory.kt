package hep.dataforge.vis.spatial.three

import hep.dataforge.names.Name
import hep.dataforge.names.startsWith
import hep.dataforge.provider.Type
import hep.dataforge.vis.common.VisualObject
import hep.dataforge.vis.spatial.*
import hep.dataforge.vis.spatial.Material3D.Companion.MATERIAL_KEY
import hep.dataforge.vis.spatial.three.ThreeFactory.Companion.TYPE
import info.laht.threekt.core.BufferGeometry
import info.laht.threekt.core.Object3D
import info.laht.threekt.objects.Mesh
import kotlin.reflect.KClass

/**
 * Builder and updater for three.js object
 */
@Type(TYPE)
interface ThreeFactory<in T : VisualObject> {

    val type: KClass<in T>

    operator fun invoke(obj: T): Object3D

    companion object {
        const val TYPE = "threeFactory"
    }
}

/**
 * Update position, rotation and visibility
 */
fun Object3D.updatePosition(obj: VisualObject3D) {
    visible = obj.visible ?: true
    position.set(obj.x, obj.y, obj.z)
    setRotationFromEuler(obj.euler)
    scale.set(obj.scaleX, obj.scaleY, obj.scaleZ)
    updateMatrix()
}

///**
// * Unsafe invocation of a factory
// */
//operator fun <T : VisualObject3D> ThreeFactory<T>.invoke(obj: Any): Object3D {
//    if (type.isInstance(obj)) {
//        @Suppress("UNCHECKED_CAST")
//        return invoke(obj as T)
//    } else {
//        error("The object of type ${obj::class} could not be rendered by this factory")
//    }
//}

/**
 * Update non-position non-geometry property
 */
fun Object3D.updateProperty(source: VisualObject, propertyName: Name) {
    if (this is Mesh && propertyName.startsWith(MATERIAL_KEY)) {
        updateMaterial(source)
    } else if (
        source is VisualObject3D &&
        (propertyName.startsWith(VisualObject3D.position)
                || propertyName.startsWith(VisualObject3D.rotation)
                || propertyName.startsWith(VisualObject3D.scale))
    ) {
        //update position of mesh using this object
        updatePosition(source)
    } else if (propertyName == VisualObject3D.VISIBLE_KEY) {
        visible = source.visible ?: true
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