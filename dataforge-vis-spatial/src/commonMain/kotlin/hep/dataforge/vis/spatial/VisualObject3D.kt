@file:UseSerializers(Point3DSerializer::class, NameSerializer::class)

package hep.dataforge.vis.spatial

import hep.dataforge.io.ConfigSerializer
import hep.dataforge.io.NameSerializer
import hep.dataforge.meta.*
import hep.dataforge.names.Name
import hep.dataforge.names.plus
import hep.dataforge.output.Output
import hep.dataforge.vis.common.VisualGroup
import hep.dataforge.vis.common.VisualObject
import hep.dataforge.vis.common.asName
import hep.dataforge.vis.spatial.VisualObject3D.Companion.DETAIL_KEY
import hep.dataforge.vis.spatial.VisualObject3D.Companion.MATERIAL_KEY
import hep.dataforge.vis.spatial.VisualObject3D.Companion.VISIBLE_KEY
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

interface VisualObject3D : VisualObject {
    var position: Point3D?
    var rotation: Point3D?
    var scale: Point3D?

    fun MetaBuilder.updatePosition() {
        xPos to position?.x
        yPos to position?.y
        zPos to position?.z
        xRotation to rotation?.x
        yRotation to rotation?.y
        zRotation to rotation?.z
        xScale to scale?.x
        yScale to scale?.y
        zScale to scale?.z
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

@Serializable
class VisualGroup3D : VisualGroup<VisualObject3D>(), VisualObject3D, Configurable {
    /**
     * A container for templates visible inside this group
     */
    var templates: VisualGroup3D? = null
        set(value) {
            value?.parent = this
            field = value
        }

    @Serializable(ConfigSerializer::class)
    override var properties: Config? = null

    override var position: Point3D? = null
    override var rotation: Point3D? = null
    override var scale: Point3D? = null

    override val namedChildren: MutableMap<Name, VisualObject3D> = HashMap()
    override val unnamedChildren: MutableList<VisualObject3D> = ArrayList()

    fun getTemplate(name: Name): VisualObject3D? = templates?.get(name) ?: (parent as? VisualGroup3D)?.getTemplate(name)

    override fun MetaBuilder.updateMeta() {
        set(TEMPLATES_KEY, templates?.toMeta())
        updatePosition()
        updateChildren()
    }

    companion object {
        const val TEMPLATES_KEY = "templates"
    }
}

fun VisualGroup3D.group(key: String? = null, action: VisualGroup3D.() -> Unit = {}): VisualGroup3D =
    VisualGroup3D().apply(action).also { set(key, it) }

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
    set(value) = setProperty(VisualObject3D.rotationOrder, value.name)


/**
 * Preferred number of polygons for displaying the object. If not defined, uses shape or renderer default. Not inherited
 */
var VisualObject3D.detail: Int?
    get() = getProperty(DETAIL_KEY, false).int
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

private fun VisualObject3D.position(): Point3D =
    position ?: Point3D(0.0, 0.0, 0.0).also { position = it }

var VisualObject3D.x: Number
    get() = position?.x ?: 0f
    set(value) {
        position().x = value.toDouble()
        propertyChanged(VisualObject3D.xPos)
    }

var VisualObject3D.y: Number
    get() = position?.y ?: 0f
    set(value) {
        position().y = value.toDouble()
        propertyChanged(VisualObject3D.yPos)
    }

var VisualObject3D.z: Number
    get() = position?.z ?: 0f
    set(value) {
        position().z = value.toDouble()
        propertyChanged(VisualObject3D.zPos)
    }

private fun VisualObject3D.rotation(): Point3D =
    rotation ?: Point3D(0.0, 0.0, 0.0).also { rotation = it }

var VisualObject3D.rotationX: Number
    get() = rotation?.x ?: 0f
    set(value) {
        rotation().x = value.toDouble()
        propertyChanged(VisualObject3D.xRotation)
    }

var VisualObject3D.rotationY: Number
    get() = rotation?.y ?: 0f
    set(value) {
        rotation().y = value.toDouble()
        propertyChanged(VisualObject3D.yRotation)
    }

var VisualObject3D.rotationZ: Number
    get() = rotation?.z ?: 0f
    set(value) {
        rotation().z = value.toDouble()
        propertyChanged(VisualObject3D.zRotation)
    }

private fun VisualObject3D.scale(): Point3D =
    scale ?: Point3D(1.0, 1.0, 1.0).also { scale = it }

var VisualObject3D.scaleX: Number
    get() = scale?.x ?: 1f
    set(value) {
        scale().x = value.toDouble()
        propertyChanged(VisualObject3D.xScale)
    }

var VisualObject3D.scaleY: Number
    get() = scale?.y ?: 1f
    set(value) {
        scale().y = value.toDouble()
        propertyChanged(VisualObject3D.yScale)
    }

var VisualObject3D.scaleZ: Number
    get() = scale?.z ?: 1f
    set(value) {
        scale().z = value.toDouble()
        propertyChanged(VisualObject3D.zScale)
    }