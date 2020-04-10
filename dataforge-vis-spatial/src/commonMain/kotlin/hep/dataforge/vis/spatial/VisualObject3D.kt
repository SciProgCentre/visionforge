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

    companion object {

        val VISIBLE_KEY = "visible".asName()

        //        val SELECTED_KEY = "selected".asName()
        val DETAIL_KEY = "detail".asName()
        val LAYER_KEY = "layer".asName()
        val IGNORE_KEY = "ignore".asName()

        val GEOMETRY_KEY = "geometry".asName()

        val x = "x".asName()
        val y = "y".asName()
        val z = "z".asName()

        val position = "pos".asName()

        val xPos = position + x
        val yPos = position + y
        val zPos = position + z

        val rotation = "rotation".asName()

        val xRotation = rotation + x
        val yRotation = rotation + y
        val zRotation = rotation + z

        val rotationOrder = rotation + "order"

        val scale = "scale".asName()

        val xScale = scale + x
        val yScale = scale + y
        val zScale = scale + z

        val descriptor by lazy {
            NodeDescriptor {
                defineValue(VISIBLE_KEY) {
                    type(ValueType.BOOLEAN)
                    default(true)
                }

                defineItem(Material3D.MATERIAL_KEY.toString(), Material3D.descriptor)

//                Material3D.MATERIAL_COLOR_KEY put "#ffffff"
//                Material3D.MATERIAL_OPACITY_KEY put 1.0
//                Material3D.MATERIAL_WIREFRAME_KEY put false

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
    get() = getProperty(VisualObject3D.rotationOrder).enum<RotationOrder>() ?: RotationOrder.XYZ
    set(value) = setProperty(VisualObject3D.rotationOrder, value.name.asValue())


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
        propertyInvalidated(VisualObject3D.xPos)
    }

var VisualObject3D.y: Number
    get() = position?.y ?: 0f
    set(value) {
        position().y = value.toDouble()
        propertyInvalidated(VisualObject3D.yPos)
    }

var VisualObject3D.z: Number
    get() = position?.z ?: 0f
    set(value) {
        position().z = value.toDouble()
        propertyInvalidated(VisualObject3D.zPos)
    }

private fun VisualObject3D.rotation(): Point3D =
    rotation ?: Point3D(0.0, 0.0, 0.0).also { rotation = it }

var VisualObject3D.rotationX: Number
    get() = rotation?.x ?: 0f
    set(value) {
        rotation().x = value.toDouble()
        propertyInvalidated(VisualObject3D.xRotation)
    }

var VisualObject3D.rotationY: Number
    get() = rotation?.y ?: 0f
    set(value) {
        rotation().y = value.toDouble()
        propertyInvalidated(VisualObject3D.yRotation)
    }

var VisualObject3D.rotationZ: Number
    get() = rotation?.z ?: 0f
    set(value) {
        rotation().z = value.toDouble()
        propertyInvalidated(VisualObject3D.zRotation)
    }

private fun VisualObject3D.scale(): Point3D =
    scale ?: Point3D(1.0, 1.0, 1.0).also { scale = it }

var VisualObject3D.scaleX: Number
    get() = scale?.x ?: 1f
    set(value) {
        scale().x = value.toDouble()
        propertyInvalidated(VisualObject3D.xScale)
    }

var VisualObject3D.scaleY: Number
    get() = scale?.y ?: 1f
    set(value) {
        scale().y = value.toDouble()
        propertyInvalidated(VisualObject3D.yScale)
    }

var VisualObject3D.scaleZ: Number
    get() = scale?.z ?: 1f
    set(value) {
        scale().z = value.toDouble()
        propertyInvalidated(VisualObject3D.zScale)
    }