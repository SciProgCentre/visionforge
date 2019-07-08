package hep.dataforge.vis.spatial

import hep.dataforge.context.Context
import hep.dataforge.meta.Meta
import hep.dataforge.output.Output
import hep.dataforge.vis.common.VisualGroup
import hep.dataforge.vis.common.VisualObject
import javafx.scene.Group
import javafx.scene.Node
import org.fxyz3d.shapes.primitives.CuboidMesh
import tornadofx.*

/**
 * https://github.com/miho/JCSG for operations
 *
 */
class FX3DOutput(override val context: Context) : Output<VisualObject> {
    val canvas by lazy { Canvas3D() }


    private fun buildNode(obj: VisualObject): Node? {
        val listener = DisplayObjectFXListener(obj)
        val x = listener["pos.x"].float()
        val y = listener["pos.y"].float()
        val z = listener["pos.z"].float()
        val center = objectBinding(x, y, z) {
            org.fxyz3d.geometry.Point3D(x.value ?: 0f, y.value ?: 0f, z.value ?: 0f)
        }
        return when (obj) {
            is VisualGroup -> Group(obj.map { buildNode(it) }).apply {
                this.translateXProperty().bind(x)
                this.translateYProperty().bind(y)
                this.translateZProperty().bind(z)
            }
            is Box -> CuboidMesh(obj.xSize, obj.ySize, obj.zSize).apply {
                this.centerProperty().bind(center)
                this.materialProperty().bind(listener["color"].transform { it.material() })
            }
            else -> {
                logger.error { "No renderer defined for ${obj::class}" }
                null
            }
        }
    }

    override fun render(obj: VisualObject, meta: Meta) {
        buildNode(obj)?.let { canvas.world.children.add(it) }
    }
}