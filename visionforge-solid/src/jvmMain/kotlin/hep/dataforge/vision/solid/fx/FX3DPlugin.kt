package hep.dataforge.vision.solid.fx

import hep.dataforge.context.*
import hep.dataforge.meta.Meta
import hep.dataforge.meta.boolean
import hep.dataforge.provider.Type
import hep.dataforge.vision.solid.*
import hep.dataforge.vision.solid.SolidMaterial.Companion.MATERIAL_KEY
import hep.dataforge.vision.solid.SolidMaterial.Companion.MATERIAL_WIREFRAME_KEY
import hep.dataforge.vision.solid.fx.FX3DFactory.Companion.TYPE
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.shape.CullFace
import javafx.scene.shape.DrawMode
import javafx.scene.shape.Shape3D
import javafx.scene.text.Font
import javafx.scene.text.Text
import javafx.scene.transform.Rotate
import org.fxyz3d.shapes.composites.PolyLine3D
import org.fxyz3d.shapes.primitives.CuboidMesh
import org.fxyz3d.shapes.primitives.SpheroidMesh
import kotlin.collections.set
import kotlin.math.PI
import kotlin.reflect.KClass

class FX3DPlugin : AbstractPlugin() {
    override val tag: PluginTag get() = Companion.tag

    private val objectFactories = HashMap<KClass<out Solid>, FX3DFactory<*>>()
    private val compositeFactory = FXCompositeFactory(this)
    private val proxyFactory = FXProxyFactory(this)

    init {
        //Add specialized factories here
        objectFactories[Convex::class] = FXConvexFactory
    }


    @Suppress("UNCHECKED_CAST")
    private fun findObjectFactory(type: KClass<out Solid>): FX3DFactory<Solid>? {
        return (objectFactories[type] ?: context.gather<FX3DFactory<*>>(TYPE).values.find { it.type == type })
                as FX3DFactory<Solid>?
    }

    fun buildNode(obj: Solid): Node {
        val binding = VisualObjectFXBinding(obj)
        return when (obj) {
            is Proxy -> proxyFactory(obj, binding)
            is SolidGroup -> {
                Group(obj.children.mapNotNull { (token, obj) ->
                    (obj as? Solid)?.let {
                        buildNode(it).apply {
                            properties["name"] = token.toString()
                        }
                    }
                })
            }
            is Composite -> compositeFactory(obj, binding)
            is Box -> CuboidMesh(obj.xSize.toDouble(), obj.ySize.toDouble(), obj.zSize.toDouble())
            is Sphere -> if (obj.phi == PI2 && obj.theta == PI.toFloat()) {
                //use sphere for orb
                SpheroidMesh(obj.detail ?: 16, obj.radius.toDouble(), obj.radius.toDouble())
            } else {
                FXShapeFactory(obj, binding)
            }
            is SolidLabel -> Text(obj.text).apply {
                font = Font.font(obj.fontFamily, obj.fontSize)
                x = -layoutBounds.width / 2
                y = layoutBounds.height / 2
            }
            is PolyLine -> PolyLine3D(
                obj.points.map { it.point },
                obj.thickness.toFloat(),
                obj.getItem(SolidMaterial.MATERIAL_COLOR_KEY)?.color()
            ).apply {
                this.meshView.cullFace = CullFace.FRONT
            }
            else -> {
                //find specialized factory for this type if it is present
                val factory: FX3DFactory<Solid>? = findObjectFactory(obj::class)
                when {
                    factory != null -> factory(obj, binding)
                    obj is GeometrySolid -> FXShapeFactory(obj, binding)
                    else -> error("Renderer for ${obj::class} not found")
                }
            }
        }.apply {
            translateXProperty().bind(binding[Solid.X_POSITION_KEY].float(obj.x.toFloat()))
            translateYProperty().bind(binding[Solid.Y_POSITION_KEY].float(obj.y.toFloat()))
            translateZProperty().bind(binding[Solid.Z_POSITION_KEY].float(obj.z.toFloat()))
            scaleXProperty().bind(binding[Solid.X_SCALE_KEY].float(obj.scaleX.toFloat()))
            scaleYProperty().bind(binding[Solid.Y_SCALE_KEY].float(obj.scaleY.toFloat()))
            scaleZProperty().bind(binding[Solid.Z_SCALE_KEY].float(obj.scaleZ.toFloat()))

            val rotateX = Rotate(0.0, Rotate.X_AXIS).apply {
                angleProperty().bind(binding[Solid.X_ROTATION_KEY].float(obj.rotationX.toFloat()).multiply(180.0 / PI))
            }

            val rotateY = Rotate(0.0, Rotate.Y_AXIS).apply {
                angleProperty().bind(binding[Solid.Y_ROTATION_KEY].float(obj.rotationY.toFloat()).multiply(180.0 / PI))
            }

            val rotateZ = Rotate(0.0, Rotate.Z_AXIS).apply {
                angleProperty().bind(binding[Solid.Z_ROTATION_KEY].float(obj.rotationZ.toFloat()).multiply(180.0 / PI))
            }

            when (obj.rotationOrder) {
                RotationOrder.ZYX -> transforms.addAll(rotateZ, rotateY, rotateX)
                RotationOrder.XZY -> transforms.addAll(rotateX, rotateZ, rotateY)
                RotationOrder.YXZ -> transforms.addAll(rotateY, rotateX, rotateZ)
                RotationOrder.YZX -> transforms.addAll(rotateY, rotateZ, rotateX)
                RotationOrder.ZXY -> transforms.addAll(rotateZ, rotateX, rotateY)
                RotationOrder.XYZ -> transforms.addAll(rotateX, rotateY, rotateZ)
            }

            if (this is Shape3D) {
                materialProperty().bind(binding[MATERIAL_KEY].transform {
                    it.material()
                })

                drawModeProperty().bind(binding[MATERIAL_WIREFRAME_KEY].transform {
                    if (it.boolean == true) {
                        DrawMode.LINE
                    } else {
                        DrawMode.FILL
                    }
                })
            }
        }
    }

    companion object : PluginFactory<FX3DPlugin> {
        override val tag = PluginTag("visual.fx3D", PluginTag.DATAFORGE_GROUP)
        override val type = FX3DPlugin::class
        override fun invoke(meta: Meta, context: Context) = FX3DPlugin()
    }
}

/**
 * Builder and updater for three.js object
 */
@Type(TYPE)
interface FX3DFactory<in T : Solid> {

    val type: KClass<in T>

    operator fun invoke(obj: T, binding: VisualObjectFXBinding): Node

    companion object {
        const val TYPE = "fx3DFactory"
    }
}

