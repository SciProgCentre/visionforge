package hep.dataforge.vision.solid

import hep.dataforge.meta.boolean
import hep.dataforge.meta.descriptors.NodeDescriptor
import hep.dataforge.meta.enum
import hep.dataforge.meta.int
import hep.dataforge.names.Name
import hep.dataforge.names.asName
import hep.dataforge.names.plus
import hep.dataforge.values.ValueType
import hep.dataforge.values.asValue
import hep.dataforge.vision.*
import hep.dataforge.vision.Vision.Companion.VISIBLE_KEY
import hep.dataforge.vision.layout.Output
import hep.dataforge.vision.solid.Solid.Companion.DETAIL_KEY
import hep.dataforge.vision.solid.Solid.Companion.IGNORE_KEY
import hep.dataforge.vision.solid.Solid.Companion.LAYER_KEY

/**
 * Interface for 3-dimensional [Vision]
 */
public interface Solid : Vision {
    public var position: Point3D?
    public var rotation: Point3D?
    public var scale: Point3D?

    override val descriptor: NodeDescriptor get() = Companion.descriptor

    public companion object {
        //        val SELECTED_KEY = "selected".asName()
        public val DETAIL_KEY: Name = "detail".asName()
        public val LAYER_KEY: Name = "layer".asName()
        public val IGNORE_KEY: Name = "ignore".asName()

        public val GEOMETRY_KEY: Name = "geometry".asName()

        public val X_KEY: Name = "x".asName()
        public val Y_KEY: Name = "y".asName()
        public val Z_KEY: Name = "z".asName()

        public val POSITION_KEY: Name = "pos".asName()

        public val X_POSITION_KEY: Name = POSITION_KEY + X_KEY
        public val Y_POSITION_KEY: Name = POSITION_KEY + Y_KEY
        public val Z_POSITION_KEY: Name = POSITION_KEY + Z_KEY

        public val ROTATION: Name = "rotation".asName()

        public val X_ROTATION_KEY: Name = ROTATION + X_KEY
        public val Y_ROTATION_KEY: Name = ROTATION + Y_KEY
        public val Z_ROTATION_KEY: Name = ROTATION + Z_KEY

        public val ROTATION_ORDER_KEY: Name = ROTATION + "order"

        public val SCALE_KEY: Name = "scale".asName()

        public val X_SCALE_KEY: Name = SCALE_KEY + X_KEY
        public val Y_SCALE_KEY: Name = SCALE_KEY + Y_KEY
        public val Z_SCALE_KEY: Name = SCALE_KEY + Z_KEY

        public val descriptor: NodeDescriptor by lazy {
            NodeDescriptor {
                value(VISIBLE_KEY) {
                    type(ValueType.BOOLEAN)
                    default(true)
                }

                //TODO replace by descriptor merge
                value(Vision.STYLE_KEY) {
                    type(ValueType.STRING)
                    multiple = true
                }

                item(SolidMaterial.MATERIAL_KEY.toString(), SolidMaterial.descriptor)

                enum(ROTATION_ORDER_KEY, default = RotationOrder.XYZ)
            }
        }

        internal fun solidEquals(first: Solid, second: Solid): Boolean {
            if (first.position != second.position) return false
            if (first.rotation != second.rotation) return false
            if (first.scale != second.scale) return false
            if (first.properties != second.properties) return false
            return true
        }

        internal fun solidHashCode(solid: Solid): Int {
            var result = +(solid.position?.hashCode() ?: 0)
            result = 31 * result + (solid.rotation?.hashCode() ?: 0)
            result = 31 * result + (solid.scale?.hashCode() ?: 0)
            result = 31 * result + solid.properties.hashCode()
            return result
        }
    }
}

/**
 * Get the layer number this solid belongs to. Return 0 if layer is not defined.
 */
public var Solid.layer: Int
    get() = properties.getItem(LAYER_KEY).int ?: 0
    set(value) {
        setProperty(LAYER_KEY, value)
    }

@VisionBuilder
public fun Output<Solid>.solidGroup(builder: SolidGroup.() -> Unit): Unit = render(SolidGroup().apply(builder))

// Common properties

public enum class RotationOrder {
    XYZ,
    YZX,
    ZXY,
    XZY,
    YXZ,
    ZYX
}

/**
 * Rotation order
 */
public var Solid.rotationOrder: RotationOrder
    get() = getProperty(Solid.ROTATION_ORDER_KEY).enum<RotationOrder>() ?: RotationOrder.XYZ
    set(value) = setProperty(Solid.ROTATION_ORDER_KEY, value.name.asValue())


/**
 * Preferred number of polygons for displaying the object. If not defined, uses shape or renderer default. Not inherited
 */
public var Solid.detail: Int?
    get() = getProperty(DETAIL_KEY, false).int
    set(value) = setProperty(DETAIL_KEY, value?.asValue())

/**
 * If this property is true, the object will be ignored on render.
 * Property is not inherited.
 */
public var Vision.ignore: Boolean?
    get() = getProperty(IGNORE_KEY, false).boolean
    set(value) = setProperty(IGNORE_KEY, value?.asValue())

//var VisualObject.selected: Boolean?
//    get() = getProperty(SELECTED_KEY).boolean
//    set(value) = setProperty(SELECTED_KEY, value)

private fun Solid.position(): Point3D =
    position ?: Point3D(0.0, 0.0, 0.0).also { position = it }

public var Solid.x: Number
    get() = position?.x ?: 0f
    set(value) {
        position().x = value.toDouble()
        notifyPropertyChanged(Solid.X_POSITION_KEY)
    }

public var Solid.y: Number
    get() = position?.y ?: 0f
    set(value) {
        position().y = value.toDouble()
        notifyPropertyChanged(Solid.Y_POSITION_KEY)
    }

public var Solid.z: Number
    get() = position?.z ?: 0f
    set(value) {
        position().z = value.toDouble()
        notifyPropertyChanged(Solid.Z_POSITION_KEY)
    }

private fun Solid.rotation(): Point3D =
    rotation ?: Point3D(0.0, 0.0, 0.0).also { rotation = it }

public var Solid.rotationX: Number
    get() = rotation?.x ?: 0f
    set(value) {
        rotation().x = value.toDouble()
        notifyPropertyChanged(Solid.X_ROTATION_KEY)
    }

public var Solid.rotationY: Number
    get() = rotation?.y ?: 0f
    set(value) {
        rotation().y = value.toDouble()
        notifyPropertyChanged(Solid.Y_ROTATION_KEY)
    }

public var Solid.rotationZ: Number
    get() = rotation?.z ?: 0f
    set(value) {
        rotation().z = value.toDouble()
        notifyPropertyChanged(Solid.Z_ROTATION_KEY)
    }

private fun Solid.scale(): Point3D =
    scale ?: Point3D(1.0, 1.0, 1.0).also { scale = it }

public var Solid.scaleX: Number
    get() = scale?.x ?: 1f
    set(value) {
        scale().x = value.toDouble()
        notifyPropertyChanged(Solid.X_SCALE_KEY)
    }

public var Solid.scaleY: Number
    get() = scale?.y ?: 1f
    set(value) {
        scale().y = value.toDouble()
        notifyPropertyChanged(Solid.Y_SCALE_KEY)
    }

public var Solid.scaleZ: Number
    get() = scale?.z ?: 1f
    set(value) {
        scale().z = value.toDouble()
        notifyPropertyChanged(Solid.Z_SCALE_KEY)
    }