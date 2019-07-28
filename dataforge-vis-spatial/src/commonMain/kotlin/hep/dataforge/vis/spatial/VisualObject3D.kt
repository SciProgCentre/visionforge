package hep.dataforge.vis.spatial

import hep.dataforge.meta.*
import hep.dataforge.names.plus
import hep.dataforge.output.Output
import hep.dataforge.vis.common.VisualGroup
import hep.dataforge.vis.common.VisualObject
import hep.dataforge.vis.common.asName

fun VisualGroup.group(key: String? = null, vararg meta: Meta, action: VisualGroup.() -> Unit = {}): VisualGroup =
    VisualGroup(this, meta).apply(action).also { set(key, it) }


fun Output<VisualObject>.render(meta: Meta = EmptyMeta, action: VisualGroup.() -> Unit) =
    render(VisualGroup().apply(action), meta)

//TODO replace properties by containers?

object PropertyNames3D {
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
}

// Common properties

/**
 * Visibility property. Inherited from parent
 */
var VisualObject.visible
    get() = properties["visible"].boolean ?: true
    set(value) {
        config["visible"] = value
    }

// 3D Object position

/**
 * x position property relative to parent. Not inherited
 */
var VisualObject.x
    get() =  config[PropertyNames3D.xPos].number ?: 0.0
    set(value) {
        config[PropertyNames3D.xPos] = value
    }


/**
 * y position property. Not inherited
 */
var VisualObject.y
    get() = config[PropertyNames3D.yPos].number ?: 0.0
    set(value) {
        config[PropertyNames3D.yPos] = value
    }


/**
 * z position property. Not inherited
 */
var VisualObject.z
    get() = config[PropertyNames3D.zPos].number ?: 0.0
    set(value) {
        config[PropertyNames3D.zPos] = value
    }

// 3D Object rotation


/**
 * x rotation relative to parent. Not inherited
 */
var VisualObject.rotationX
    get() = config[PropertyNames3D.xRotation].number ?: 0.0
    set(value) {
        config[PropertyNames3D.xRotation] = value
    }

/**
 * y rotation relative to parent. Not inherited
 */
var VisualObject.rotationY
    get() = config[PropertyNames3D.yRotation].number ?: 0.0
    set(value) {
        config[PropertyNames3D.yRotation] = value
    }

/**
 * z rotation relative to parent. Not inherited
 */
var VisualObject.rotationZ
    get() = config[PropertyNames3D.zRotation].number ?: 0.0
    set(value) {
        config[PropertyNames3D.zRotation] = value
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
    get() = config[PropertyNames3D.rotationOrder].enum<RotationOrder>() ?: RotationOrder.XYZ
    set(value) {
        config[PropertyNames3D.rotationOrder] = value
    }

// 3D object scale

/**
 * X scale. Not inherited
 */
var VisualObject.scaleX
    get() = config[PropertyNames3D.xScale].number ?: 1.0
    set(value) {
        config[PropertyNames3D.xScale] = value
    }

/**
 * Y scale. Not inherited
 */
var VisualObject.scaleY
    get() = config[PropertyNames3D.yScale].number ?: 1.0
    set(value) {
        config[PropertyNames3D.yScale] = value
    }

/**
 * Z scale. Not inherited
 */
var VisualObject.scaleZ
    get() = config[PropertyNames3D.zScale].number ?: 1.0
    set(value) {
        config[PropertyNames3D.zScale] = value
    }

//TODO add inherited scale

/**
 * Preferred number of polygons for displaying the object. If not defined, uses shape or renderer default
 */
var VisualObject.detail: Int?
    get() = properties["detail"]?.int
    set(value) {
        config["detail"] = value
    }

object World {
    const val CAMERA_INITIAL_DISTANCE = -500.0
    const val CAMERA_INITIAL_X_ANGLE = -50.0
    const val CAMERA_INITIAL_Y_ANGLE = 0.0
    const val CAMERA_INITIAL_Z_ANGLE = -210.0
    const val CAMERA_NEAR_CLIP = 0.1
    const val CAMERA_FAR_CLIP = 10000.0
}