package hep.dataforge.vision.solid.three

import hep.dataforge.context.*
import hep.dataforge.meta.Meta
import hep.dataforge.names.*
import hep.dataforge.vision.Vision
import hep.dataforge.vision.solid.*
import info.laht.threekt.core.Object3D
import kotlin.collections.set
import kotlin.reflect.KClass
import info.laht.threekt.objects.Group as ThreeGroup

class ThreePlugin : AbstractPlugin() {
    override val tag: PluginTag get() = Companion.tag

    private val objectFactories = HashMap<KClass<out Solid>, ThreeFactory<*>>()
    private val compositeFactory = ThreeCompositeFactory(this)
    private val proxyFactory = ThreeProxyFactory(this)

    init {
        //Add specialized factories here
        objectFactories[Box::class] = ThreeBoxFactory
        objectFactories[Convex::class] = ThreeConvexFactory
        objectFactories[Sphere::class] = ThreeSphereFactory
        objectFactories[ConeSegment::class] = ThreeCylinderFactory
        objectFactories[PolyLine::class] = ThreeLineFactory
        objectFactories[SolidLabel::class] = ThreeCanvasLabelFactory
    }

    @Suppress("UNCHECKED_CAST")
    private fun findObjectFactory(type: KClass<out Vision>): ThreeFactory<Solid>? {
        return (objectFactories[type]
            ?: context.content<ThreeFactory<*>>(ThreeFactory.TYPE).values.find { it.type == type })
                as ThreeFactory<Solid>?
    }

    fun buildObject3D(obj: Solid): Object3D {
        return when (obj) {
            is ThreeVision -> obj.toObject3D()
            is Proxy -> proxyFactory(obj)
            is SolidGroup -> {
                val group = ThreeGroup()
                obj.children.forEach { (token, child) ->
                    if (child is Solid && child.ignore != true) {
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

                    obj.onPropertyChange(this) { name->
                        if (
                            name.startsWith(Solid.POSITION_KEY) ||
                            name.startsWith(Solid.ROTATION) ||
                            name.startsWith(Solid.SCALE_KEY)
                        ) {
                            //update position of mesh using this object
                            updatePosition(obj)
                        } else if (name == Solid.VISIBLE_KEY) {
                            visible = obj.visible ?: true
                        }
                    }

                    obj.onChildrenChange(this) { name, child ->
                        if (name.isEmpty()) {
                            logger.error { "Children change with empty name on $group" }
                            return@onChildrenChange
                        }

//                        val parentName = name.cutLast()
//                        val childName = name.last()!!

                        //removing old object
                        findChild(name)?.let { oldChild ->
                            oldChild.parent?.remove(oldChild)
                        }

                        //adding new object
                        if (child != null && child is Solid) {
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
                val factory: ThreeFactory<Solid>? = findObjectFactory(obj::class)
                when {
                    factory != null -> factory(obj)
                    obj is GeometrySolid -> ThreeShapeFactory(obj)
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