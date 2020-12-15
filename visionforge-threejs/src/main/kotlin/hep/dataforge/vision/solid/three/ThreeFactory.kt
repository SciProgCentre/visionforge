package hep.dataforge.vision.solid.three

import hep.dataforge.names.Name
import hep.dataforge.names.startsWith
import hep.dataforge.provider.Type
import hep.dataforge.vision.Vision
import hep.dataforge.vision.solid.*
import hep.dataforge.vision.solid.SolidMaterial.Companion.MATERIAL_KEY
import hep.dataforge.vision.solid.three.ThreeFactory.Companion.TYPE
import hep.dataforge.vision.solid.three.ThreeMaterials.getMaterial
import hep.dataforge.vision.visible
import info.laht.threekt.core.BufferGeometry
import info.laht.threekt.core.Object3D
import info.laht.threekt.objects.Mesh
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
    if(obj is Solid) {
        position.set(obj.x, obj.y, obj.z)
        setRotationFromEuler(obj.euler)
        scale.set(obj.scaleX, obj.scaleY, obj.scaleZ)
        updateMatrix()
    }
}

/**
 * Update non-position non-geometry property
 */
public fun Object3D.updateProperty(source: Vision, propertyName: Name) {
    if (this is Mesh && propertyName.startsWith(MATERIAL_KEY)) {
        this.material = getMaterial(source, false)
    } else if (
        propertyName.startsWith(Solid.POSITION_KEY)
        || propertyName.startsWith(Solid.ROTATION)
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
    override fun buildGeometry(obj: GeometrySolid): BufferGeometry {
        return obj.run {
            ThreeGeometryBuilder().apply { toGeometry(this) }.build()
        }
    }
}