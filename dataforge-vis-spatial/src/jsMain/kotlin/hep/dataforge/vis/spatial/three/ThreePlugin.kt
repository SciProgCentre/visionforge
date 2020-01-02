package hep.dataforge.vis.spatial.three

import hep.dataforge.context.*
import hep.dataforge.meta.Meta
import hep.dataforge.names.*
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
        objectFactories[Label3D::class] = ThreeLabelFactory
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
                obj.children.forEach { (token, child) ->
                    if (child is VisualObject3D && child.ignore != true) {
                        try {
                            val object3D = buildObject3D(child)
                            group[token] = object3D
                        } catch (ex: Throwable) {
                            logger.error(ex) { "Failed to render $child" }
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

                    obj.onChildrenChange(this) { name, child ->
                        if (name.isEmpty()) {
                            logger.error { "Children change with empty namr on $group" }
                            return@onChildrenChange
                        }

                        val parentName = name.cutLast()
                        val childName = name.last()!!

                        //removing old object
                        findChild(name)?.let { oldChild ->
                            oldChild.parent?.remove(oldChild)
                        }

                        //adding new object
                        if (child != null && child is VisualObject3D) {
                            try {
                                val object3D = buildObject3D(child)
                                set(name, object3D)
                            } catch (ex: Throwable) {
                                logger.error(ex) { "Failed to render $child" }
                            }
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

internal operator fun Object3D.set(token: NameToken, object3D: Object3D) {
    object3D.name = token.toString()
    add(object3D)
}

internal fun Object3D.getOrCreateGroup(name: Name): Object3D {
    return when {
        name.isEmpty() -> this
        name.length == 1 -> {
            val token = name.first()!!
            children.find { it.name == token.toString() } ?: info.laht.threekt.objects.Group().also { group ->
                group.name = token.toString()
                this.add(group)
            }
        }
        else -> getOrCreateGroup(name.first()!!.asName()).getOrCreateGroup(name.cutFirst())
    }
}

internal operator fun Object3D.set(name: Name, obj: Object3D) {
    when (name.length) {
        0 -> error("Can't set object with an empty name")
        1 -> set(name.first()!!, obj)
        else -> getOrCreateGroup(name.cutLast())[name.last()!!] = obj
    }
}

internal fun Object3D.findChild(name: Name): Object3D? {
    return when {
        name.isEmpty() -> this
        name.length == 1 -> this.children.find { it.name == name.first()!!.toString() }
        else -> findChild(name.first()!!.asName())?.findChild(name.cutFirst())
    }
}