package hep.dataforge.vis.spatial

import hep.dataforge.meta.*
import hep.dataforge.names.Name
import hep.dataforge.names.plus
import hep.dataforge.output.Output
import hep.dataforge.vis.common.VisualGroup
import hep.dataforge.vis.common.VisualLeaf
import hep.dataforge.vis.common.VisualObject
import hep.dataforge.vis.common.asName
import hep.dataforge.vis.spatial.VisualObject3D.Companion.detailKey
import hep.dataforge.vis.spatial.VisualObject3D.Companion.materialKey
import hep.dataforge.vis.spatial.VisualObject3D.Companion.visibleKey

data class Value3(var x: Float = 0f, var y: Float = 0f, var z: Float = 0f)

interface VisualObject3D : VisualObject {
    var position: Value3
    var rotation: Value3
    var scale: Value3

    fun setProperty(name: Name, value: Any?)
    fun getProperty(name: Name, inherit: Boolean = true): MetaItem<*>?

    companion object {
        val materialKey = "material".asName()
        val visibleKey = "visible".asName()
        val detailKey = "detail".asName()

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

open class VisualLeaf3D(parent: VisualObject?, tagRefs: Array<out Meta>) : VisualLeaf(parent, tagRefs), VisualObject3D {
    override var position: Value3 = Value3()
    override var rotation: Value3 = Value3()
    override var scale: Value3 = Value3(1f, 1f, 1f)

    private var _config: Config? = null
    override val config: Config get() = _config ?: Config().also { _config = it }

    override fun setProperty(name: Name, value: Any?) {
        config[name] = value
    }

    override fun getProperty(name: Name, inherit: Boolean): MetaItem<*>? {
        return if (inherit) {
            config[name] ?: (parent as? VisualObject3D)?.getProperty(name, inherit) ?: parent?.properties[name]
        } else {
            _config?.get(name)
        }
    }
}

class VisualGroup3D(
    parent: VisualObject? = null,
    tagRefs: Array<out Meta> = emptyArray()
) : VisualGroup(parent, tagRefs), VisualObject3D {

    override var position: Value3 = Value3()
    override var rotation: Value3 = Value3()
    override var scale: Value3 = Value3(1f, 1f, 1f)

    private var _config: Config? = null
    override val config: Config get() = _config ?: Config().also { _config = it }

    override fun setProperty(name: Name, value: Any?) {
        config[name] = value
    }

    override fun getProperty(name: Name, inherit: Boolean): MetaItem<*>? {
        return if (inherit) {
            config[name] ?: (parent as? VisualObject3D)?.getProperty(name, inherit) ?: parent?.properties[name]
        } else {
            _config?.get(name)
        }
    }
}


fun VisualGroup.group(key: String? = null, vararg meta: Meta, action: VisualGroup3D.() -> Unit = {}): VisualGroup3D =
    VisualGroup3D(this, meta).apply(action).also { set(key, it) }


fun Output<VisualObject>.render(meta: Meta = EmptyMeta, action: VisualGroup3D.() -> Unit) =
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
 * Preferred number of polygons for displaying the object. If not defined, uses shape or renderer default
 */
var VisualObject3D.detail: Int?
    get() = getProperty(detailKey).int
    set(value) = setProperty(detailKey, value)

var VisualObject3D.material: Meta?
    get() = getProperty(materialKey).node
    set(value) = setProperty(materialKey, value)

var VisualObject3D.visible: Boolean?
    get() = getProperty(visibleKey).boolean
    set(value) = setProperty(visibleKey, value)

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

object World {
    const val CAMERA_INITIAL_DISTANCE = -500.0
    const val CAMERA_INITIAL_X_ANGLE = -50.0
    const val CAMERA_INITIAL_Y_ANGLE = 0.0
    const val CAMERA_INITIAL_Z_ANGLE = -210.0
    const val CAMERA_NEAR_CLIP = 0.1
    const val CAMERA_FAR_CLIP = 10000.0
}