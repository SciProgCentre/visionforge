package hep.dataforge.vis.spatial.three

import hep.dataforge.context.AbstractPlugin
import hep.dataforge.context.PluginFactory
import hep.dataforge.context.PluginTag
import hep.dataforge.context.content
import hep.dataforge.meta.*
import hep.dataforge.names.Name
import hep.dataforge.names.asName
import hep.dataforge.names.isEmpty
import hep.dataforge.names.startsWith
import hep.dataforge.vis.spatial.*
import info.laht.threekt.cameras.Camera
import info.laht.threekt.cameras.PerspectiveCamera
import info.laht.threekt.core.Object3D
import info.laht.threekt.external.controls.OrbitControls
import info.laht.threekt.external.controls.TrackballControls
import org.w3c.dom.Node
import kotlin.collections.set
import kotlin.reflect.KClass
import info.laht.threekt.objects.Group as ThreeGroup

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
        objectFactories[ConeSegment::class] = ThreeCylinderFactory
    }

    private fun findObjectFactory(type: KClass<out VisualObject3D>): ThreeFactory<*>? {
        return objectFactories[type]
            ?: context.content<ThreeFactory<*>>(ThreeFactory.TYPE).values.find { it.type == type }
    }

    fun buildObject3D(obj: VisualObject3D): Object3D {
        return when (obj) {
            is Proxy -> proxyFactory(obj)
            is VisualGroup3D -> {
                val group = ThreeGroup()
                obj.children.forEach { (name, child) ->
                    if (child is VisualObject3D && child.ignore != true) {
                        try {
                            val object3D = buildObject3D(child)
                            object3D.name = name.toString()
                            group.add(object3D)
                        } catch (ex: Throwable) {
                            logger.error(ex) { "Failed to render $name" }
                        }
                    }
                }

                group.apply {
                    updatePosition(obj)
                    //obj.onChildrenChange()

                    obj.onPropertyChange(this) { name, _, _ ->
                        if (
                            name.startsWith(VisualObject3D.position) ||
                            name.startsWith(VisualObject3D.rotation) ||
                            name.startsWith(VisualObject3D.scale)
                        ) {
                            //update position of mesh using this object
                            updatePosition(obj)
                        } else if (name == VisualObject3D.VISIBLE_KEY) {
                            visible = obj.visible ?: true
                        }
                    }
                }
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
        override val tag = PluginTag("visual.three", PluginTag.DATAFORGE_GROUP)
        override val type = ThreePlugin::class
        override fun invoke(meta: Meta) = ThreePlugin()
    }
}

fun Object3D.findChild(name: Name): Object3D? {
    return when {
        name.isEmpty() -> this
        name.length == 1 -> this.children.find { it.name == name.first()!!.toString() }
        else -> findChild(name.first()!!.asName())?.findChild(name.cutFirst())
    }
}