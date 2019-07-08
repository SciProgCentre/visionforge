package hep.dataforge.vis.spatial

import hep.dataforge.meta.*
import hep.dataforge.output.Output
import hep.dataforge.vis.common.VisualGroup
import hep.dataforge.vis.common.VisualObject
import hep.dataforge.vis.common.getProperty

fun VisualGroup.group(meta: Meta = EmptyMeta, action: VisualGroup.() -> Unit = {}): VisualGroup =
    VisualGroup(this, meta).apply(action).also { add(it) }


fun Output<VisualObject>.render(meta: Meta = EmptyMeta, action: VisualGroup.() -> Unit) =
    render(VisualGroup(null, EmptyMeta).apply(action), meta)

//TODO replace properties by containers?

// Common properties

/**
 * Visibility property. Inherited from parent
 */
var VisualObject.visible
    get() = getProperty("visible").boolean ?: true
    set(value) {
        properties["visible"] = value
    }

// 3D Object position

/**
 * x position property relative to parent. Not inherited
 */
var VisualObject.x
    get() = properties["pos.x"].number ?: 0.0
    set(value) {
        properties["pos.x"] = value
    }

/**
 * y position property. Not inherited
 */
var VisualObject.y
    get() = properties["pos.y"].number ?: 0.0
    set(value) {
        properties["pos.y"] = value
    }

/**
 * z position property. Not inherited
 */
var VisualObject.z
    get() = properties["pos.z"].number ?: 0.0
    set(value) {
        properties["pos.z"] = value
    }

// 3D Object rotation

/**
 * x rotation relative to parent. Not inherited
 */
var VisualObject.rotationX
    get() = properties["rotation.x"].number ?: 0.0
    set(value) {
        properties["rotation.x"] = value
    }

/**
 * y rotation relative to parent. Not inherited
 */
var VisualObject.rotationY
    get() = properties["rotation.y"].number ?: 0.0
    set(value) {
        properties["rotation.y"] = value
    }

/**
 * z rotation relative to parent. Not inherited
 */
var VisualObject.rotationZ
    get() = properties["rotation.z"].number ?: 0.0
    set(value) {
        properties["rotation.z"] = value
    }

enum class RotationOrder {
    XYZ,
    YZX,
    ZXY,
    XZY,
    YXZ,
    ZYX
}

/**
 * Rotation order. Not inherited
 */
var VisualObject.rotationOrder: RotationOrder
    get() = getProperty("rotation.order").enum<RotationOrder>() ?: RotationOrder.XYZ
    set(value) {
        properties["rotation.order"] = value
    }

// 3D object scale

/**
 * X scale. Not inherited
 */
var VisualObject.scaleX
    get() = properties["scale.x"].number ?: 1.0
    set(value) {
        properties["scale.x"] = value
    }

/**
 * Y scale. Not inherited
 */
var VisualObject.scaleY
    get() = properties["scale.y"].number ?: 1.0
    set(value) {
        properties["scale.y"] = value
    }

/**
 * Z scale. Not inherited
 */
var VisualObject.scaleZ
    get() = properties["scale.z"].number ?: 1.0
    set(value) {
        properties["scale.z"] = value
    }

//TODO add inherited scale

/**
 * Preferred number of polygons for displaying the object. If not defined, uses shape or renderer default
 */
var VisualObject.detail: Int?
    get() = properties["detail"]?.int
    set(value) {
        properties["detail"] = value
    }

object World {
    const val CAMERA_INITIAL_DISTANCE = -500.0
    const val CAMERA_INITIAL_X_ANGLE = -50.0
    const val CAMERA_INITIAL_Y_ANGLE = 0.0
    const val CAMERA_INITIAL_Z_ANGLE = -210.0
    const val CAMERA_NEAR_CLIP = 0.1
    const val CAMERA_FAR_CLIP = 10000.0
}