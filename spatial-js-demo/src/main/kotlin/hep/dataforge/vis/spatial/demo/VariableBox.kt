@file:UseSerializers(Point3DSerializer::class)

package hep.dataforge.vis.spatial.demo

import hep.dataforge.meta.int
import hep.dataforge.meta.number
import hep.dataforge.names.plus
import hep.dataforge.vis.common.getProperty
import hep.dataforge.vis.common.setProperty
import hep.dataforge.vis.spatial.*
import hep.dataforge.vis.spatial.VisualObject3D.Companion.GEOMETRY_KEY
import hep.dataforge.vis.spatial.demo.VariableBoxThreeFactory.X_SIZE_KEY
import hep.dataforge.vis.spatial.demo.VariableBoxThreeFactory.Y_SIZE_KEY
import hep.dataforge.vis.spatial.demo.VariableBoxThreeFactory.Z_SIZE_KEY
import hep.dataforge.vis.spatial.three.CustomThreeVisualObject
import hep.dataforge.vis.spatial.three.MeshThreeFactory
import info.laht.threekt.core.BufferGeometry
import info.laht.threekt.geometries.BoxBufferGeometry
import kotlinx.serialization.UseSerializers
import kotlin.math.max

private val BOX_Z_SIZE_KEY = GEOMETRY_KEY + "zSize"

internal var VisualObject3D.variableZSize: Number
    get() = getProperty(BOX_Z_SIZE_KEY, false).number ?: 0f
    set(value) {
        setProperty(BOX_Z_SIZE_KEY, value)
    }

internal var VisualObject3D.value: Int
    get() = getProperty("value", false).int ?: 0
    set(value) {
        setProperty("value", value)
        val size = value.toFloat() / 255f * 20f
        variableZSize = size
        z = -size / 2

        val b = max(0, 255 - value)
        val r = max(0, value - 255)
        val g = 255 - b - r
        color(r.toUByte(), g.toUByte(), b.toUByte())
    }

fun VisualGroup3D.varBox(
    xSize: Number,
    ySize: Number,
    zSize: Number,
    name: String = "",
    action: VisualObject3D.() -> Unit = {}
) = CustomThreeVisualObject(VariableBoxThreeFactory).apply {
    setProperty(X_SIZE_KEY, xSize)
    setProperty(Y_SIZE_KEY, ySize)
    setProperty(Z_SIZE_KEY, zSize)
}.apply(action).also { set(name, it) }

private object VariableBoxThreeFactory : MeshThreeFactory<VisualObject3D>(VisualObject3D::class) {
    val X_SIZE_KEY = GEOMETRY_KEY + "xSize"
    val Y_SIZE_KEY = GEOMETRY_KEY + "ySize"
    val Z_SIZE_KEY = GEOMETRY_KEY + "zSize"

    override fun buildGeometry(obj: VisualObject3D): BufferGeometry {
        val xSize = obj.getProperty(X_SIZE_KEY, false).number ?: 0f
        val ySize = obj.getProperty(Y_SIZE_KEY, false).number ?: 0f
        val zSize = obj.getProperty(Z_SIZE_KEY, false).number ?: 0f
        return obj.detail?.let { detail ->
            BoxBufferGeometry(xSize, ySize, zSize, detail, detail, detail)
        } ?: BoxBufferGeometry(xSize, ySize, zSize)
    }
}