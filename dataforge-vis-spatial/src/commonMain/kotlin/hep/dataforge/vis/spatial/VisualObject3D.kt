package hep.dataforge.vis.spatial

import hep.dataforge.meta.*
import hep.dataforge.names.plus
import hep.dataforge.output.Output
import hep.dataforge.vis.common.AbstractVisualObject
import hep.dataforge.vis.common.VisualGroup
import hep.dataforge.vis.common.VisualObject
import hep.dataforge.vis.common.asName
import hep.dataforge.vis.spatial.VisualObject3D.Companion.DETAIL_KEY
import hep.dataforge.vis.spatial.VisualObject3D.Companion.MATERIAL_KEY
import hep.dataforge.vis.spatial.VisualObject3D.Companion.VISIBLE_KEY

data class Value3(var x: Float = 0f, var y: Float = 0f, var z: Float = 0f)

interface VisualObject3D : VisualObject {
    var position: Value3
    var rotation: Value3
    var scale: Value3

    fun MetaBuilder.updatePosition() {
        xPos to position.x
        yPos to position.y
        zPos to position.z
        xRotation to rotation.x
        yRotation to rotation.y
        zRotation to rotation.z
        xScale to scale.x
        yScale to scale.y
        zScale to scale.z
    }

    companion object {
        val MATERIAL_KEY = "material".asName()
        val VISIBLE_KEY = "visible".asName()
        val DETAIL_KEY = "detail".asName()

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
}

abstract class VisualLeaf3D(parent: VisualObject?) : AbstractVisualObject(parent), VisualObject3D, Configurable {
    override var position: Value3 = Value3()
    override var rotation: Value3 = Value3()
    override var scale: Value3 = Value3(1f, 1f, 1f)
}

class VisualGroup3D(parent: VisualObject? = null) : VisualGroup<VisualObject3D>(parent), VisualObject3D, Configurable {

    override var position: Value3 = Value3()
    override var rotation: Value3 = Value3()
    override var scale: Value3 = Value3(1f, 1f, 1f)

    override fun MetaBuilder.updateMeta() {
        updatePosition()
        updateChildren()
    }
}

fun VisualGroup3D.group(key: String? = null, action: VisualGroup3D.() -> Unit = {}): VisualGroup3D =
    VisualGroup3D(this).apply(action).also { set(key, it) }

fun Output<VisualObject3D>.render(meta: Meta = EmptyMeta, action: VisualGroup3D.() -> Unit) =
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
    set(value) = setProperty(VisualObject3D.rotationOrder, value)


/**
 * Preferred number of polygons for displaying the object. If not defined, uses shape or renderer default. Not inherited
 */
var VisualObject3D.detail: Int?
    get() = getProperty(DETAIL_KEY,false).int
    set(value) = setProperty(DETAIL_KEY, value)

var VisualObject3D.material: Meta?
    get() = getProperty(MATERIAL_KEY).node
    set(value) = setProperty(MATERIAL_KEY, value)

var VisualObject3D.visible: Boolean?
    get() = getProperty(VISIBLE_KEY).boolean
    set(value) = setProperty(VISIBLE_KEY, value)

fun VisualObject3D.color(rgb: Int) {
    material = buildMeta { "color" to rgb }
}

fun VisualObject3D.material(builder: MetaBuilder.() -> Unit) {
    material = buildMeta(builder)
}

fun VisualObject3D.color(r: Int, g: Int, b: Int) = material {
    "red" to r
    "green" to g
    "blue" to b
}

var VisualObject3D.x: Number
    get() = position.x
    set(value) {
        position.x = value.toFloat()
        propertyChanged(VisualObject3D.xPos)
    }

var VisualObject3D.y: Number
    get() = position.y
    set(value) {
        position.y = value.toFloat()
        propertyChanged(VisualObject3D.yPos)
    }

var VisualObject3D.z: Number
    get() = position.z
    set(value) {
        position.z = value.toFloat()
        propertyChanged(VisualObject3D.zPos)
    }

var VisualObject3D.rotationX: Number
    get() = rotation.x
    set(value) {
        rotation.x = value.toFloat()
        propertyChanged(VisualObject3D.xRotation)
    }

var VisualObject3D.rotationY: Number
    get() = rotation.y
    set(value) {
        rotation.y = value.toFloat()
        propertyChanged(VisualObject3D.xRotation)
    }

var VisualObject3D.rotationZ: Number
    get() = rotation.z
    set(value) {
        rotation.z = value.toFloat()
        propertyChanged(VisualObject3D.zRotation)
    }

var VisualObject3D.scaleX: Number
    get() = scale.x
    set(value) {
        scale.x = value.toFloat()
        propertyChanged(VisualObject3D.xScale)
    }

var VisualObject3D.scaleY: Number
    get() = scale.y
    set(value) {
        scale.y = value.toFloat()
        propertyChanged(VisualObject3D.yScale)
    }

var VisualObject3D.scaleZ: Number
    get() = scale.z
    set(value) {
        scale.z = value.toFloat()
        propertyChanged(VisualObject3D.zScale)
    }

