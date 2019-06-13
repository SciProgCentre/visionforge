package hep.dataforge.vis.spatial

import hep.dataforge.meta.*
import hep.dataforge.output.Output
import hep.dataforge.vis.DisplayGroup
import hep.dataforge.vis.DisplayObject
import hep.dataforge.vis.DisplayObjectList
import hep.dataforge.vis.getProperty

fun DisplayObjectList.group(meta: Meta = EmptyMeta, action: DisplayObjectList.() -> Unit = {}): DisplayGroup =
    DisplayObjectList(this, meta).apply(action).also { addChild(it) }


fun Output<DisplayObject>.render(meta: Meta = EmptyMeta, action: DisplayObjectList.() -> Unit) =
    render(DisplayObjectList(null, EmptyMeta).apply(action), meta)

//TODO replace properties by containers?

// Common properties

/**
 * Visibility property. Inherited from parent
 */
var DisplayObject.visible
    get() = getProperty("visible").boolean ?: true
    set(value) {
        properties["visible"] = value
    }

// 3D Object position

/**
 * x position property relative to parent. Not inherited
 */
var DisplayObject.x
    get() = properties["pos.x"].number ?: 0.0
    set(value) {
        properties["pos.x"] = value
    }

/**
 * y position property. Not inherited
 */
var DisplayObject.y
    get() = properties["pos.y"].number ?: 0.0
    set(value) {
        properties["pos.y"] = value
    }

/**
 * z position property. Not inherited
 */
var DisplayObject.z
    get() = properties["pos.z"].number ?: 0.0
    set(value) {
        properties["pos.z"] = value
    }

// 3D Object rotation

/**
 * x rotation relative to parent. Not inherited
 */
var DisplayObject.rotationX
    get() = properties["rotation.x"].number ?: 0.0
    set(value) {
        properties["rotation.x"] = value
    }

/**
 * y rotation relative to parent. Not inherited
 */
var DisplayObject.rotationY
    get() = properties["rotation.y"].number ?: 0.0
    set(value) {
        properties["rotation.y"] = value
    }

/**
 * z rotation relative to parent. Not inherited
 */
var DisplayObject.rotationZ
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
var DisplayObject.rotationOrder: RotationOrder
    get() = properties["rotation.order"].enum<RotationOrder>() ?: RotationOrder.XYZ
    set(value) {
        properties["rotation.order"] = value
    }

// 3D object scale

/**
 * X scale. Not inherited
 */
var DisplayObject.scaleX
    get() = properties["scale.x"].number ?: 1.0
    set(value) {
        properties["scale.x"] = value
    }

/**
 * Y scale. Not inherited
 */
var DisplayObject.scaleY
    get() = properties["scale.y"].number ?: 1.0
    set(value) {
        properties["scale.y"] = value
    }

/**
 * Z scale. Not inherited
 */
var DisplayObject.scaleZ
    get() = properties["scale.z"].number ?: 1.0
    set(value) {
        properties["scale.z"] = value
    }

fun DisplayObject.color(rgb: Int) {
    this.properties["color"] = rgb
}

fun DisplayObject.color(meta: Meta) {
    this.properties["color"] = meta
}

fun DisplayObject.color(r: Int, g: Int, b: Int) = color(buildMeta {
    "red" to r
    "green" to g
    "blue" to b
})


//TODO add inherited scale

object World {
    const val CAMERA_INITIAL_DISTANCE = -500.0
    const val CAMERA_INITIAL_X_ANGLE = -50.0
    const val CAMERA_INITIAL_Y_ANGLE = 0.0
    const val CAMERA_INITIAL_Z_ANGLE = -210.0
    const val CAMERA_NEAR_CLIP = 0.1
    const val CAMERA_FAR_CLIP = 10000.0
}