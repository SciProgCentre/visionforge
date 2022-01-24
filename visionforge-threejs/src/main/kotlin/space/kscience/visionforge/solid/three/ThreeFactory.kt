package space.kscience.visionforge.solid.three

import info.laht.threekt.core.BufferGeometry
import info.laht.threekt.core.Object3D
import info.laht.threekt.math.Euler
import info.laht.threekt.objects.Mesh
import space.kscience.dataforge.misc.Type
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.startsWith
import space.kscience.visionforge.Vision
import space.kscience.visionforge.computeProperty
import space.kscience.visionforge.solid.*
import space.kscience.visionforge.solid.SolidMaterial.Companion.MATERIAL_KEY
import space.kscience.visionforge.solid.three.ThreeFactory.Companion.TYPE
import space.kscience.visionforge.visible
import kotlin.reflect.KClass

/**
 * Builder and updater for three.js object
 */
@Type(TYPE)
public interface ThreeFactory<in T : Vision> {

    public val type: KClass<in T>

    public operator fun invoke(three: ThreePlugin, obj: T): Object3D

    public companion object {
        public const val TYPE: String = "threeFactory"
    }
}

/**
 * Update position, rotation and visibility
 */
public fun Object3D.updatePosition(obj: Vision) {
    visible = obj.visible ?: true
    if (obj is Solid) {
        position.set(obj.x, obj.y, obj.z)

//        val quaternion = obj.quaternion
//
//        if (quaternion != null) {
//            val (x, y, z, w) = quaternion
//            setRotationFromQuaternion(Quaternion(x, y, z, w))
//        } else {
//            setRotationFromEuler( Euler(obj.rotationX, obj.rotationY, obj.rotationZ, obj.rotationOrder.name))
//        }

        setRotationFromEuler( Euler(obj.rotationX, obj.rotationY, obj.rotationZ, obj.rotationOrder.name))

        scale.set(obj.scaleX, obj.scaleY, obj.scaleZ)
        updateMatrix()
    }
}

/**
 * Update non-position non-geometry property
 */
public fun Object3D.updateProperty(source: Vision, propertyName: Name) {
    console.log("$source updated $propertyName with ${source.computeProperty(propertyName)}")
    if (this is Mesh && propertyName.startsWith(MATERIAL_KEY)) {
        updateMaterialProperty(source, propertyName)
    } else if (
        propertyName.startsWith(Solid.POSITION_KEY)
        || propertyName.startsWith(Solid.ROTATION_KEY)
        || propertyName.startsWith(Solid.SCALE_KEY)
    ) {
        //update position of mesh using this object
        updatePosition(source)
    } else if (propertyName == Vision.VISIBLE_KEY) {
        visible = source.visible ?: true
    }
}

/**
 * Generic factory for elements which provide inside geometry builder
 */
public object ThreeShapeFactory : MeshThreeFactory<GeometrySolid>(GeometrySolid::class) {
    override fun buildGeometry(obj: GeometrySolid): BufferGeometry = ThreeGeometryBuilder().apply {
        obj.toGeometry(this)
    }.build()
}