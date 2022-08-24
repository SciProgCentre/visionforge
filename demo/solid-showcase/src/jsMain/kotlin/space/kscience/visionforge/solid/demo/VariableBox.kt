package space.kscience.visionforge.solid.demo

import space.kscience.dataforge.meta.asValue
import space.kscience.dataforge.meta.int
import space.kscience.dataforge.meta.number
import space.kscience.dataforge.names.asName
import space.kscience.dataforge.names.startsWith
import space.kscience.visionforge.onPropertyChange
import space.kscience.visionforge.setChild
import space.kscience.visionforge.solid.SolidGroup
import space.kscience.visionforge.solid.SolidMaterial.Companion.EDGES_KEY
import space.kscience.visionforge.solid.layer
import space.kscience.visionforge.solid.three.*
import three.core.Object3D
import three.geometries.BoxGeometry
import three.objects.Mesh
import kotlin.math.max

internal fun SolidGroup.varBox(
    xSize: Number,
    ySize: Number,
    name: String = "",
    action: VariableBox.() -> Unit = {},
): VariableBox = VariableBox(xSize, ySize).apply(action).also { setChild(name, it) }

internal class VariableBox(val xSize: Number, val ySize: Number) : ThreeJsVision() {

    override fun render(three: ThreePlugin): Object3D {
        val geometry = BoxGeometry(xSize, ySize, 1)

        val material = ThreeMaterials.DEFAULT.clone()

        val mesh = Mesh(geometry, material).apply {
            //updateMaterial(this@VariableBox)
            applyEdges(this@VariableBox)
            //applyWireFrame(this@VariableBox)

            //set position for mesh
            updatePosition(this@VariableBox)

            layers.enable(this@VariableBox.layer)
            children.forEach {
                it.layers.enable(this@VariableBox.layer)
            }
        }
        mesh.scale.z = properties.getValue(VALUE)?.number?.toDouble() ?: 1.0

        //add listener to object properties
        onPropertyChange { name ->
            when {
                name == VALUE -> {
                    val value = properties.getValue(VALUE)?.int ?: 0
                    val size = value.toFloat() / 255f * 20f
                    mesh.scale.z = size.toDouble()
                    mesh.position.z = size.toDouble() / 2

                    val b = max(0, 128 - value)
                    val r = max(0, value - 128)
                    val g = 255 - b - r
                    material.color.setRGB(r.toFloat() / 256, g.toFloat() / 256, b.toFloat() / 256)
                    mesh.updateMatrix()
                }

                name.startsWith(EDGES_KEY) -> mesh.applyEdges(this@VariableBox)
                else -> mesh.updateProperty(this@VariableBox, name)
            }
        }

        return mesh
    }

    var value: Int
        get() = properties.getValue(VALUE)?.int ?: 0
        set(value) {
            properties.setValue(VALUE, value.asValue())
        }

    companion object {
        private val VALUE = "value".asName()
//
//        private val X_SIZE_KEY = GEOMETRY_KEY + "xSize"
//        private val Y_SIZE_KEY = GEOMETRY_KEY + "ySize"
//        private val Z_SIZE_KEY = GEOMETRY_KEY + "zSize"
    }
}