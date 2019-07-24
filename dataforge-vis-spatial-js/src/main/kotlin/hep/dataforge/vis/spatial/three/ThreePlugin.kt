package hep.dataforge.vis.spatial.three

import hep.dataforge.context.AbstractPlugin
import hep.dataforge.context.PluginFactory
import hep.dataforge.context.PluginTag
import hep.dataforge.context.content
import hep.dataforge.meta.*
import hep.dataforge.vis.common.VisualGroup
import hep.dataforge.vis.common.VisualObject
import hep.dataforge.vis.spatial.*
import info.laht.threekt.cameras.Camera
import info.laht.threekt.cameras.PerspectiveCamera
import info.laht.threekt.core.Object3D
import info.laht.threekt.external.controls.OrbitControls
import info.laht.threekt.external.controls.TrackballControls
import org.w3c.dom.Node
import kotlin.collections.set
import kotlin.reflect.KClass

class ThreePlugin : AbstractPlugin() {
    override val tag: PluginTag get() = Companion.tag

    private val objectFactories = HashMap<KClass<out VisualObject>, ThreeFactory<*>>()
    private val compositeFactory = ThreeCompositeFactory(this)

    init {
        //Add specialized factories here
        objectFactories[Box::class] = ThreeBoxFactory
        objectFactories[Convex::class] = ThreeConvexFactory
        objectFactories[Sphere::class] = ThreeSphereFactory
        objectFactories[Cylinder::class] = ThreeCylinderFactory
    }

    private fun findObjectFactory(type: KClass<out VisualObject>): ThreeFactory<*>? {
        return objectFactories[type]
            ?: context.content<ThreeFactory<*>>(ThreeFactory.TYPE).values.find { it.type == type }
    }

    fun buildObject3D(obj: VisualObject): Object3D {
        return when (obj) {
            is VisualGroup -> Group(obj.map { buildObject3D(it) }).apply {
                updatePosition(obj)
            }
            is Composite -> compositeFactory(obj)
            else -> {
                //find specialized factory for this type if it is present
                val factory = findObjectFactory(obj::class)
                when {
                    factory != null -> factory(obj)
                    obj is Shape -> ThreeShapeFactory(obj)
                    else -> error("Renderer for ${obj::class} not found")
                }
            }
        }
    }

    fun buildCamera(meta: Meta) = PerspectiveCamera(
        meta["fov"].int ?: 75,
        meta["aspect"].double ?: 1.0,
        meta["nearClip"].double ?: World.CAMERA_NEAR_CLIP,
        meta["farClip"].double ?: World.CAMERA_FAR_CLIP
    ).apply {
        position.setZ(World.CAMERA_INITIAL_DISTANCE)
        rotation.set(
            World.CAMERA_INITIAL_X_ANGLE,
            World.CAMERA_INITIAL_Y_ANGLE,
            World.CAMERA_INITIAL_Z_ANGLE
        )
    }

    fun addControls(camera: Camera, element: Node, meta: Meta) {
        when (meta["type"].string) {
            "trackball" -> TrackballControls(camera, element)
            else -> OrbitControls(camera, element)
        }
    }

    companion object : PluginFactory<ThreePlugin> {
        override val tag = PluginTag("vis.three", "hep.dataforge")
        override val type = ThreePlugin::class
        override fun invoke(meta: Meta) = ThreePlugin()
    }
}