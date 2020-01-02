@file:UseSerializers(Point3DSerializer::class)

package hep.dataforge.vis.spatial.demo

import hep.dataforge.meta.int
import hep.dataforge.meta.number
import hep.dataforge.names.plus
import hep.dataforge.names.startsWith
import hep.dataforge.vis.common.getProperty
import hep.dataforge.vis.common.setProperty
import hep.dataforge.vis.spatial.*
import hep.dataforge.vis.spatial.VisualObject3D.Companion.GEOMETRY_KEY
import hep.dataforge.vis.spatial.demo.VariableBoxThreeFactory.Z_SIZE_KEY
import hep.dataforge.vis.spatial.three.*
import info.laht.threekt.core.BufferGeometry
import info.laht.threekt.core.Object3D
import info.laht.threekt.geometries.BoxBufferGeometry
import info.laht.threekt.materials.MeshBasicMaterial
import info.laht.threekt.objects.Mesh
import kotlinx.serialization.UseSerializers
import kotlin.math.max
import kotlin.reflect.KClass

internal var VisualObject3D.variableZSize: Number
    get() = getProperty(Z_SIZE_KEY, false).number ?: 0f
    set(value) {
        setProperty(Z_SIZE_KEY, value)
    }

internal var VisualObject3D.value: Int
    get() = getProperty("value", false).int ?: 0
    set(value) {
        setProperty("value", value)
        val size = value.toFloat() / 255f * 20f
        scaleZ = size
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
    scaleX = xSize
    scaleY = ySize
    scaleZ = zSize
}.apply(action).also { set(name, it) }

private object VariableBoxThreeFactory : ThreeFactory<VisualObject3D> {
    val X_SIZE_KEY = GEOMETRY_KEY + "xSize"
    val Y_SIZE_KEY = GEOMETRY_KEY + "ySize"
    val Z_SIZE_KEY = GEOMETRY_KEY + "zSize"

    override val type: KClass<in VisualObject3D> get() = VisualObject3D::class

    override fun invoke(obj: VisualObject3D): Object3D {
        val xSize = obj.getProperty(X_SIZE_KEY, false).number?.toDouble() ?: 1.0
        val ySize = obj.getProperty(Y_SIZE_KEY, false).number?.toDouble() ?: 1.0
        val zSize = obj.getProperty(Z_SIZE_KEY, false).number?.toDouble() ?: 1.0
        val geometry = BoxBufferGeometry(1, 1, 1)

        //JS sometimes tries to pass Geometry as BufferGeometry
        @Suppress("USELESS_IS_CHECK") if (geometry !is BufferGeometry) error("BufferGeometry expected")

        val mesh = Mesh(geometry, MeshBasicMaterial()).apply {
            applyEdges(obj)
            applyWireFrame(obj)

            //set position for mesh
            updatePosition(obj)

            //set color for mesh
            updateMaterial(obj)

            layers.enable(obj.layer)
            children.forEach {
                it.layers.enable(obj.layer)
            }
        }

        mesh.scale.set(xSize, ySize, zSize)

        //add listener to object properties
        obj.onPropertyChange(this) { name, _, _ ->
            when {
//                name.startsWith(GEOMETRY_KEY) -> {
//                    val newXSize = obj.getProperty(X_SIZE_KEY, false).number?.toDouble() ?: 1.0
//                    val newYSize = obj.getProperty(Y_SIZE_KEY, false).number?.toDouble() ?: 1.0
//                    val newZSize = obj.getProperty(Z_SIZE_KEY, false).number?.toDouble() ?: 1.0
//                    mesh.scale.set(newXSize, newYSize, newZSize)
//                    mesh.updateMatrix()
//                }
                name.startsWith(MeshThreeFactory.WIREFRAME_KEY) -> mesh.applyWireFrame(obj)
                name.startsWith(MeshThreeFactory.EDGES_KEY) -> mesh.applyEdges(obj)
                else -> mesh.updateProperty(obj, name)
            }
        }
        return mesh
    }
}