@file:UseSerializers(Point3DSerializer::class)

package hep.dataforge.vis.spatial

import hep.dataforge.meta.*
import hep.dataforge.meta.descriptors.NodeDescriptor
import hep.dataforge.names.asName
import hep.dataforge.names.plus
import hep.dataforge.output.Renderer
import hep.dataforge.values.ValueType
import hep.dataforge.values.asValue
import hep.dataforge.vis.VisualObject
import hep.dataforge.vis.enum
import hep.dataforge.vis.spatial.VisualObject3D.Companion.DETAIL_KEY
import hep.dataforge.vis.spatial.VisualObject3D.Companion.IGNORE_KEY
import hep.dataforge.vis.spatial.VisualObject3D.Companion.LAYER_KEY
import hep.dataforge.vis.spatial.VisualObject3D.Companion.VISIBLE_KEY
import kotlinx.serialization.UseSerializers

/**
 * Interface for 3-dimensional [VisualObject]
 */
interface VisualObject3D : VisualObject {
    var position: Point3D?
    var rotation: Point3D?
    var scale: Point3D?

    override val descriptor: NodeDescriptor? get() = Companion.descriptor

    companion object {

        val VISIBLE_KEY = "visible".asName()

        //        val SELECTED_KEY = "selected".asName()
        val DETAIL_KEY = "detail".asName()
        val LAYER_KEY = "layer".asName()
        val IGNORE_KEY = "ignore".asName()

        val GEOMETRY_KEY = "geometry".asName()

        val X_KEY = "x".asName()
        val Y_KEY = "y".asName()
        val Z_KEY = "z".asName()

        val POSITION_KEY = "pos".asName()

        val X_POSITION_KEY = POSITION_KEY + X_KEY
        val Y_POSITION_KEY = POSITION_KEY + Y_KEY
        val Z_POSITION_KEY = POSITION_KEY + Z_KEY

        val ROTATION = "rotation".asName()

        val X_ROTATION_KEY = ROTATION + X_KEY
        val Y_ROTATION_KEY = ROTATION + Y_KEY
        val Z_ROTATION_KEY = ROTATION + Z_KEY

        val ROTATION_ORDER_KEY = ROTATION + "order"

        val SCALE_KEY = "scale".asName()

        val X_SCALE_KEY = SCALE_KEY + X_KEY
        val Y_SCALE_KEY = SCALE_KEY + Y_KEY
        val Z_SCALE_KEY = SCALE_KEY + Z_KEY

        val descriptor by lazy {
            NodeDescriptor {
                value(VISIBLE_KEY) {
                    type(ValueType.BOOLEAN)
                    default(true)
                }

                //TODO replace by descriptor merge
                value(VisualObject.STYLE_KEY) {
                    type(ValueType.STRING)
                    multiple = true
                }

                item(Material3D.MATERIAL_KEY.toString(), Material3D.descriptor)

                enum<RotationOrder>(ROTATION_ORDER_KEY,default = RotationOrder.XYZ)
            }
        }
    }
}

/**
 * Count number of layers to the top object. Return 1 if this is top layer
 */
var VisualObject3D.layer: Int
    get() = getProperty(LAYER_KEY).int ?: 0
    set(value) {
        setProperty(LAYER_KEY, value.asValue())
    }

fun Renderer<VisualObject3D>.render(meta: Meta = Meta.EMPTY, action: VisualGroup3D.() -> Unit) =
    render(VisualGroup3D().apply(action), meta)

// Common properties

enum class RotationOrder {
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
var VisualObject3D.rotationOrder: RotationOrder
    get() = getProperty(VisualObject3D.ROTATION_ORDER_KEY).enum<RotationOrder>() ?: RotationOrder.XYZ
    set(value) = setProperty(VisualObject3D.ROTATION_ORDER_KEY, value.name.asValue())


/**
 * Preferred number of polygons for displaying the object. If not defined, uses shape or renderer default. Not inherited
 */
var VisualObject3D.detail: Int?
    get() = getProperty(DETAIL_KEY, false).int
    set(value) = setProperty(DETAIL_KEY, value?.asValue())

var VisualObject.visible: Boolean?
    get() = getProperty(VISIBLE_KEY).boolean
    set(value) = setProperty(VISIBLE_KEY, value?.asValue())

/**
 * If this property is true, the object will be ignored on render.
 * Property is not inherited.
 */
var VisualObject.ignore: Boolean?
    get() = getProperty(IGNORE_KEY, false).boolean
    set(value) = setProperty(IGNORE_KEY, value?.asValue())

//var VisualObject.selected: Boolean?
//    get() = getProperty(SELECTED_KEY).boolean
//    set(value) = setProperty(SELECTED_KEY, value)

private fun VisualObject3D.position(): Point3D =
    position ?: Point3D(0.0, 0.0, 0.0).also { position = it }

var VisualObject3D.x: Number
    get() = position?.x ?: 0f
    set(value) {
        position().x = value.toDouble()
        propertyInvalidated(VisualObject3D.X_POSITION_KEY)
    }

var VisualObject3D.y: Number
    get() = position?.y ?: 0f
    set(value) {
        position().y = value.toDouble()
        propertyInvalidated(VisualObject3D.Y_POSITION_KEY)
    }

var VisualObject3D.z: Number
    get() = position?.z ?: 0f
    set(value) {
        position().z = value.toDouble()
        propertyInvalidated(VisualObject3D.Z_POSITION_KEY)
    }

private fun VisualObject3D.rotation(): Point3D =
    rotation ?: Point3D(0.0, 0.0, 0.0).also { rotation = it }

var VisualObject3D.rotationX: Number
    get() = rotation?.x ?: 0f
    set(value) {
        rotation().x = value.toDouble()
        propertyInvalidated(VisualObject3D.X_ROTATION_KEY)
    }

var VisualObject3D.rotationY: Number
    get() = rotation?.y ?: 0f
    set(value) {
        rotation().y = value.toDouble()
        propertyInvalidated(VisualObject3D.Y_ROTATION_KEY)
    }

var VisualObject3D.rotationZ: Number
    get() = rotation?.z ?: 0f
    set(value) {
        rotation().z = value.toDouble()
        propertyInvalidated(VisualObject3D.Z_ROTATION_KEY)
    }

private fun VisualObject3D.scale(): Point3D =
    scale ?: Point3D(1.0, 1.0, 1.0).also { scale = it }

var VisualObject3D.scaleX: Number
    get() = scale?.x ?: 1f
    set(value) {
        scale().x = value.toDouble()
        propertyInvalidated(VisualObject3D.X_SCALE_KEY)
    }

var VisualObject3D.scaleY: Number
    get() = scale?.y ?: 1f
    set(value) {
        scale().y = value.toDouble()
        propertyInvalidated(VisualObject3D.Y_SCALE_KEY)
    }

var VisualObject3D.scaleZ: Number
    get() = scale?.z ?: 1f
    set(value) {
        scale().z = value.toDouble()
        propertyInvalidated(VisualObject3D.Z_SCALE_KEY)
    }