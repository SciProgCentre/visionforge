package space.kscience.visionforge.solid.three

import space.kscience.dataforge.misc.Type
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.startsWith
import space.kscience.visionforge.Vision
import space.kscience.visionforge.solid.*
import space.kscience.visionforge.solid.SolidMaterial.Companion.MATERIAL_KEY
import space.kscience.visionforge.solid.three.ThreeFactory.Companion.TYPE
import space.kscience.visionforge.visible
import three.core.BufferGeometry
import three.core.Object3D
import three.math.Euler
import three.math.Quaternion
import kotlin.reflect.KClass

/**
 * Builder and updater for three.js object
 */
@Type(TYPE)
public interface ThreeFactory<in T : Vision> {

    public val type: KClass<in T>

    /**
     * Build an [Object3D] from [vision].
     * @param observe if false, does not observe the changes in [vision] after render (useful for statics).
     */
    public suspend fun build(three: ThreePlugin, vision: T, observe: Boolean = true): Object3D

    public companion object {
        public const val TYPE: String = "threeFactory"
    }
}

/**
 * Update position, rotation and visibility
 */
public fun Object3D.updatePosition(vision: Vision) {
//    visible = vision.visible ?: true
    if (vision is Solid) {
        position.set(vision.x, vision.y, vision.z)

        val quaternion = vision.quaternionValue

        if (quaternion != null) {
            setRotationFromQuaternion(
                Quaternion(
                    quaternion.x,
                    quaternion.y,
                    quaternion.z,
                    quaternion.w
                )
            )
        } else {
            setRotationFromEuler(Euler(vision.rotationX, vision.rotationY, vision.rotationZ, vision.rotationOrder.name))
        }

        scale.set(vision.scaleX, vision.scaleY, vision.scaleZ)
        updateMatrix()
    }
}

/**
 * Update non-position non-geometry property
 */
public fun Object3D.updateProperty(source: Vision, propertyName: Name) {
    // console.log("$source updated $propertyName with ${source.computeProperty(propertyName)}")
    if (isMesh(this) && propertyName.startsWith(MATERIAL_KEY)) {
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
public object ThreeShapeFactory : ThreeMeshFactory<GeometrySolid>(GeometrySolid::class) {
    override suspend fun buildGeometry(obj: GeometrySolid): BufferGeometry = ThreeGeometryBuilder().apply {
        obj.toGeometry(this)
    }.build()
}