package hep.dataforge.vis.spatial.three

import hep.dataforge.context.*
import hep.dataforge.meta.Meta
import hep.dataforge.names.Name
import hep.dataforge.names.asName
import hep.dataforge.names.isEmpty
import hep.dataforge.names.startsWith
import hep.dataforge.vis.common.VisualObject
import hep.dataforge.vis.spatial.*
import info.laht.threekt.core.Object3D
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
        objectFactories[PolyLine::class] = ThreeLineFactory
    }

    @Suppress("UNCHECKED_CAST")
    private fun findObjectFactory(type: KClass<out VisualObject>): ThreeFactory<VisualObject3D>? {
        return (objectFactories[type]
            ?: context.content<ThreeFactory<*>>(ThreeFactory.TYPE).values.find { it.type == type })
                as ThreeFactory<VisualObject3D>?
    }

    fun buildObject3D(obj: VisualObject3D): Object3D {
        return when (obj) {
            is ThreeVisualObject -> obj.toObject3D()
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
                val factory: ThreeFactory<VisualObject3D>? = findObjectFactory(obj::class)
                when {
                    factory != null -> factory(obj)
                    obj is Shape -> ThreeShapeFactory(obj)
                    else -> error("Renderer for ${obj::class} not found")
                }
            }
        }
    }

    companion object : PluginFactory<ThreePlugin> {
        override val tag = PluginTag("visual.three", PluginTag.DATAFORGE_GROUP)
        override val type = ThreePlugin::class
        override fun invoke(meta: Meta, context: Context) = ThreePlugin()
    }
}

fun Object3D.findChild(name: Name): Object3D? {
    return when {
        name.isEmpty() -> this
        name.length == 1 -> this.children.find { it.name == name.first()!!.toString() }
        else -> findChild(name.first()!!.asName())?.findChild(name.cutFirst())
    }
}