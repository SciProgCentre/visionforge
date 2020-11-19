package hep.dataforge.vision.solid.three

import hep.dataforge.context.*
import hep.dataforge.meta.Meta
import hep.dataforge.meta.empty
import hep.dataforge.meta.invoke
import hep.dataforge.names.*
import hep.dataforge.vision.Vision
import hep.dataforge.vision.html.HtmlVisionBinding
import hep.dataforge.vision.solid.*
import hep.dataforge.vision.solid.specifications.Canvas3DOptions
import hep.dataforge.vision.visible
import info.laht.threekt.core.Object3D
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import kotlin.collections.set
import kotlin.reflect.KClass
import info.laht.threekt.objects.Group as ThreeGroup

public class ThreePlugin : AbstractPlugin(), HtmlVisionBinding<Solid> {
    override val tag: PluginTag get() = Companion.tag

    public val solidManager: SolidManager by require(SolidManager)

    private val objectFactories = HashMap<KClass<out Solid>, ThreeFactory<*>>()
    private val compositeFactory = ThreeCompositeFactory(this)
    private val refFactory = ThreeReferenceFactory(this)

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
            ?: context.gather<ThreeFactory<*>>(ThreeFactory.TYPE).values.find { it.type == type })
                as ThreeFactory<Solid>?
    }

    public fun buildObject3D(obj: Solid): Object3D {
        return when (obj) {
            is ThreeVision -> obj.render()
            is SolidReference -> refFactory(obj)
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

                    obj.onPropertyChange(this) { name ->
                        if (
                            name.startsWith(Solid.POSITION_KEY) ||
                            name.startsWith(Solid.ROTATION) ||
                            name.startsWith(Solid.SCALE_KEY)
                        ) {
                            //update position of mesh using this object
                            updatePosition(obj)
                        } else if (name == Vision.VISIBLE_KEY) {
                            visible = obj.visible ?: true
                        }
                    }

                    obj.onStructureChange(this) { nameToken, _, child ->
//                        if (name.isEmpty()) {
//                            logger.error { "Children change with empty name on $group" }
//                            return@onChildrenChange
//                        }

//                        val parentName = name.cutLast()
//                        val childName = name.last()!!

                        //removing old object
                        findChild(nameToken.asName())?.let { oldChild ->
                            oldChild.parent?.remove(oldChild)
                        }

                        //adding new object
                        if (child != null && child is Solid) {
                            try {
                                val object3D = buildObject3D(child)
                                set(nameToken, object3D)
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

    public fun createCanvas(
        element: Element,
        options: Canvas3DOptions = Canvas3DOptions.empty(),
    ): ThreeCanvas = ThreeCanvas(this, options).apply {
        attach(element)
    }

    override fun bind(element: Element, vision: Solid) {
        createCanvas(element).render(vision)
    }

    public companion object : PluginFactory<ThreePlugin> {
        override val tag: PluginTag = PluginTag("visual.three", PluginTag.DATAFORGE_GROUP)
        override val type: KClass<ThreePlugin> = ThreePlugin::class
        override fun invoke(meta: Meta, context: Context): ThreePlugin = ThreePlugin()
    }
}

public fun ThreePlugin.render(
    element: HTMLElement,
    obj: Solid,
    options: Canvas3DOptions.() -> Unit = {},
): ThreeCanvas = createCanvas(element, Canvas3DOptions(options)).apply { render(obj) }

internal operator fun Object3D.set(token: NameToken, object3D: Object3D) {
    object3D.name = token.toString()
    add(object3D)
}

internal fun Object3D.getOrCreateGroup(name: Name): Object3D {
    return when {
        name.isEmpty() -> this
        name.length == 1 -> {
            val token = name.tokens.first()
            children.find { it.name == token.toString() } ?: info.laht.threekt.objects.Group().also { group ->
                group.name = token.toString()
                this.add(group)
            }
        }
        else -> getOrCreateGroup(name.tokens.first().asName()).getOrCreateGroup(name.cutFirst())
    }
}

internal operator fun Object3D.set(name: Name, obj: Object3D) {
    when (name.length) {
        0 -> error("Can't set object with an empty name")
        1 -> set(name.tokens.first(), obj)
        else -> getOrCreateGroup(name.cutLast())[name.tokens.last()] = obj
    }
}

internal fun Object3D.findChild(name: Name): Object3D? {
    return when {
        name.isEmpty() -> this
        name.length == 1 -> this.children.find { it.name == name.tokens.first().toString() }
        else -> findChild(name.tokens.first().asName())?.findChild(name.cutFirst())
    }
}