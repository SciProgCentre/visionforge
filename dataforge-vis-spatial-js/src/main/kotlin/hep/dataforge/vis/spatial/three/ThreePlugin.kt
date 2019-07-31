package hep.dataforge.vis.spatial.three

import hep.dataforge.context.AbstractPlugin
import hep.dataforge.context.PluginFactory
import hep.dataforge.context.PluginTag
import hep.dataforge.context.content
import hep.dataforge.meta.*
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

    private val objectFactories = HashMap<KClass<out VisualObject3D>, ThreeFactory<*>>()
    private val compositeFactory = ThreeCompositeFactory(this)
    private val proxyFactory = ThreeProxyFactory(this)

    init {
        //Add specialized factories here
        objectFactories[Box::class] = ThreeBoxFactory
        objectFactories[Convex::class] = ThreeConvexFactory
        objectFactories[Sphere::class] = ThreeSphereFactory
        objectFactories[Cylinder::class] = ThreeCylinderFactory
    }

    private fun findObjectFactory(type: KClass<out VisualObject3D>): ThreeFactory<*>? {
        return objectFactories[type]
            ?: context.content<ThreeFactory<*>>(ThreeFactory.TYPE).values.find { it.type == type }
    }

    fun buildObject3D(obj: VisualObject3D): Object3D {
        return when (obj) {
            is VisualGroup3D -> Group(obj.mapNotNull {
                try {
                    buildObject3D(it)
                } catch (ex: Throwable) {
                    console.error(ex)
                    logger.error(ex) { "Failed to render $it" }
                    null
                }
            }).apply {
                updatePosition(obj)
            }
            is Composite -> compositeFactory(obj)
            is Proxy3D -> proxyFactory(obj)
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