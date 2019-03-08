package hep.dataforge.vis.spatial

import hep.dataforge.io.Output
import hep.dataforge.meta.*
import hep.dataforge.vis.DisplayGroup
import hep.dataforge.vis.DisplayNode
import hep.dataforge.vis.DisplayObject
import hep.dataforge.vis.DisplayObject.Companion.DEFAULT_TYPE

fun DisplayGroup.group(meta: Meta = EmptyMeta, action: DisplayGroup.() -> Unit = {}) =
    DisplayNode(this, DEFAULT_TYPE, meta).apply(action).also { addChild(it) }


fun Output<DisplayObject>.render(meta: Meta = EmptyMeta, action: DisplayGroup.() -> Unit) =
    render(DisplayNode(null, DEFAULT_TYPE, EmptyMeta).apply(action), meta)

//TODO replace properties by containers?

// Common properties

var DisplayObject.visible
    get() = properties["visible"].boolean ?: true
    set(value) {
        properties.style["visible"] = value
    }

// 3D Object position

var DisplayObject.x
    get() = properties["pos.x"].number ?: 0.0
    set(value) {
        properties.style["pos.x"] = value
    }

var DisplayObject.y
    get() = properties["pos.y"].number ?: 0.0
    set(value) {
        properties.style["pos.y"] = value
    }

var DisplayObject.z
    get() = properties["pos.z"].number ?: 0.0
    set(value) {
        properties.style["pos.z"] = value
    }

// 3D Object rotation

var DisplayObject.rotationX
    get() = properties["rotation.x"].number ?: 0.0
    set(value) {
        properties.style["rotation.x"] = value
    }

var DisplayObject.rotationY
    get() = properties["rotation.y"].number ?: 0.0
    set(value) {
        properties.style["rotation.y"] = value
    }

var DisplayObject.rotationZ
    get() = properties["rotation.z"].number ?: 0.0
    set(value) {
        properties.style["rotation.z"] = value
    }

enum class RotationOrder {
    XYZ,
    YZX,
    ZXY,
    XZY,
    YXZ,
    ZYX
}

var DisplayObject.rotationOrder: RotationOrder
    get() = properties["rotation.order"].enum<RotationOrder>() ?: RotationOrder.XYZ
    set(value) {
        properties.style["rotation.order"] = value
    }

// 3D object scale

var DisplayObject.scaleX
    get() = properties["scale.x"].number ?: 1.0
    set(value) {
        properties.style["scale.x"] = value
    }

var DisplayObject.scaleY
    get() = properties["scale.y"].number ?: 1.0
    set(value) {
        properties.style["scale.y"] = value
    }

var DisplayObject.scaleZ
    get() = properties["scale.z"].number ?: 1.0
    set(value) {
        properties.style["scale.z"] = value
    }

object World {
    const val CAMERA_INITIAL_DISTANCE = -500.0
    const val CAMERA_INITIAL_X_ANGLE = -50.0
    const val CAMERA_INITIAL_Y_ANGLE = 0.0
    const val CAMERA_INITIAL_Z_ANGLE = -210.0
    const val CAMERA_NEAR_CLIP = 0.1
    const val CAMERA_FAR_CLIP = 10000.0
}