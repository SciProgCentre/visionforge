@file:UseSerializers(Point3DSerializer::class)

package hep.dataforge.vision.solid.demo

import hep.dataforge.meta.int
import hep.dataforge.meta.number
import hep.dataforge.meta.set
import hep.dataforge.names.plus
import hep.dataforge.names.startsWith
import hep.dataforge.values.asValue
import hep.dataforge.vision.getProperty
import hep.dataforge.vision.set
import hep.dataforge.vision.setProperty
import hep.dataforge.vision.solid.*
import hep.dataforge.vision.solid.Solid.Companion.GEOMETRY_KEY
import hep.dataforge.vision.solid.SolidMaterial.Companion.MATERIAL_COLOR_KEY
import hep.dataforge.vision.solid.three.*
import hep.dataforge.vision.solid.three.ThreeMaterials.getMaterial
import info.laht.threekt.core.BufferGeometry
import info.laht.threekt.core.Object3D
import info.laht.threekt.geometries.BoxBufferGeometry
import info.laht.threekt.objects.Mesh
import kotlinx.serialization.UseSerializers
import kotlin.math.max

internal fun SolidGroup.varBox(
    xSize: Number,
    ySize: Number,
    zSize: Number,
    name: String = "",
    action: VariableBox.() -> Unit = {},
): VariableBox = VariableBox(xSize, ySize, zSize).apply(action).also { set(name, it) }

internal class VariableBox(xSize: Number, ySize: Number, zSize: Number) : ThreeVision() {
    init {
        scaleX = xSize
        scaleY = ySize
        scaleZ = zSize
        config[MeshThreeFactory.EDGES_ENABLED_KEY] = false
        config[MeshThreeFactory.WIREFRAME_ENABLED_KEY] = false
    }

    override fun render(): Object3D {
        val xSize = getProperty(X_SIZE_KEY, false).number?.toDouble() ?: 1.0
        val ySize = getProperty(Y_SIZE_KEY, false).number?.toDouble() ?: 1.0
        val zSize = getProperty(Z_SIZE_KEY, false).number?.toDouble() ?: 1.0
        val geometry = BoxBufferGeometry(1, 1, 1)

        //JS sometimes tries to pass Geometry as BufferGeometry
        @Suppress("USELESS_IS_CHECK") if (geometry !is BufferGeometry) error("BufferGeometry expected")

        val mesh = Mesh(geometry, getMaterial(this@VariableBox, true)).apply {
            applyEdges(this@VariableBox)
            applyWireFrame(this@VariableBox)

            //set position for mesh
            updatePosition(this@VariableBox)

            layers.enable(this@VariableBox.layer)
            children.forEach {
                it.layers.enable(this@VariableBox.layer)
            }
        }

        mesh.scale.set(xSize, ySize, zSize)

        //add listener to object properties
        onPropertyChange(mesh) { name ->
            when {
                name.startsWith(GEOMETRY_KEY) -> {
                    val newXSize = getProperty(X_SIZE_KEY, false).number?.toDouble() ?: 1.0
                    val newYSize = getProperty(Y_SIZE_KEY, false).number?.toDouble() ?: 1.0
                    val newZSize = getProperty(Z_SIZE_KEY, false).number?.toDouble() ?: 1.0
                    mesh.scale.set(newXSize, newYSize, newZSize)
                    mesh.updateMatrix()
                }
                name.startsWith(MeshThreeFactory.WIREFRAME_KEY) -> mesh.applyWireFrame(this@VariableBox)
                name.startsWith(MeshThreeFactory.EDGES_KEY) -> mesh.applyEdges(this@VariableBox)
                name.startsWith(MATERIAL_COLOR_KEY)->{
                    mesh.material = getMaterial(this, true)
                }
                else -> mesh.updateProperty(this@VariableBox, name)
            }
        }
        return mesh
    }

    var variableZSize: Number
        get() = getProperty(Z_SIZE_KEY, false).number ?: 0f
        set(value) {
            setProperty(Z_SIZE_KEY, value.asValue())
        }

    var value: Int
        get() = getProperty("value", false).int ?: 0
        set(value) {
            setProperty("value", value.asValue())
            val size = value.toFloat() / 255f * 20f
            scaleZ = size
            z = size / 2

            val b = max(0, 128 - value)
            val r = max(0, value - 128)
            val g = 255 - b - r
            color(r.toUByte(), g.toUByte(), b.toUByte())
        }

    companion object{
        private val X_SIZE_KEY = GEOMETRY_KEY + "xSize"
        private val Y_SIZE_KEY = GEOMETRY_KEY + "ySize"
        private val Z_SIZE_KEY = GEOMETRY_KEY + "zSize"
    }
}