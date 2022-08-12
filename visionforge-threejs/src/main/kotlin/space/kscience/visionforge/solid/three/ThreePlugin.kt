package space.kscience.visionforge.solid.three

import info.laht.threekt.core.Object3D
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import space.kscience.dataforge.context.*
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.update
import space.kscience.dataforge.names.*
import space.kscience.visionforge.ElementVisionRenderer
import space.kscience.visionforge.Vision
import space.kscience.visionforge.solid.*
import space.kscience.visionforge.solid.specifications.Canvas3DOptions
import space.kscience.visionforge.visible
import kotlin.collections.set
import kotlin.reflect.KClass
import info.laht.threekt.objects.Group as ThreeGroup

public class ThreePlugin : AbstractPlugin(), ElementVisionRenderer {
    override val tag: PluginTag get() = Companion.tag

    public val solids: Solids by require(Solids)

    private val objectFactories = HashMap<KClass<out Solid>, ThreeFactory<*>>()
    private val compositeFactory = ThreeCompositeFactory(this)

//    internal val updateScope: CoroutineScope get() = context

    init {
        //Add specialized factories here
        objectFactories[Box::class] = ThreeBoxFactory
        objectFactories[Convex::class] = ThreeConvexFactory
        objectFactories[Sphere::class] = ThreeSphereFactory
        objectFactories[ConeSegment::class] = ThreeConeFactory
        objectFactories[PolyLine::class] = ThreeLineFactory
        objectFactories[SolidLabel::class] = ThreeCanvasLabelFactory
        objectFactories[AmbientLightSource::class] = ThreeAmbientLightFactory
        objectFactories[PointLightSource::class] = ThreePointLightFactory
    }

    @Suppress("UNCHECKED_CAST")
    private fun findObjectFactory(type: KClass<out Vision>): ThreeFactory<Solid>? {
        return (objectFactories[type]
            ?: context.gather<ThreeFactory<*>>(ThreeFactory.TYPE).values.find { it.type == type })
                as ThreeFactory<Solid>?
    }

    public fun buildObject3D(obj: Solid): Object3D = when (obj) {
        is ThreeJsVision -> obj.render(this)
        is SolidReference -> ThreeReferenceFactory.build(this, obj)
        is SolidGroup -> {
            val group = ThreeGroup()
            obj.items.forEach { (token, child) ->
                if (token != SolidGroup.PROTOTYPES_TOKEN && child.ignore != true) {
                    try {
                        val object3D = buildObject3D(child)
                        group[token] = object3D
                    } catch (ex: Throwable) {
                        logger.error(ex) { "Failed to render $child" }
                        ex.printStackTrace()
                    }
                }
            }

            group.apply {
                updatePosition(obj)
                //obj.onChildrenChange()

                obj.properties.changes.onEach { name ->
                    if (
                        name.startsWith(Solid.POSITION_KEY) ||
                        name.startsWith(Solid.ROTATION_KEY) ||
                        name.startsWith(Solid.SCALE_KEY)
                    ) {
                        //update position of mesh using this object
                        updatePosition(obj)
                    } else if (name == Vision.VISIBLE_KEY) {
                        visible = obj.visible ?: true
                    }
                }.launchIn(context)

                obj.children.changes.onEach { childName ->
                    val child = obj.children[childName]

                    //removing old object
                    findChild(childName)?.let { oldChild ->
                        oldChild.parent?.remove(oldChild)
                    }

                    //adding new object
                    if (child != null && child is Solid) {
                        try {
                            val object3D = buildObject3D(child)
                            set(childName, object3D)
                        } catch (ex: Throwable) {
                            logger.error(ex) { "Failed to render $child" }
                        }
                    }
                }.launchIn(context)
            }
        }
        is Composite -> compositeFactory.build(this, obj)
        else -> {
            //find specialized factory for this type if it is present
            val factory: ThreeFactory<Solid>? = findObjectFactory(obj::class)
            when {
                factory != null -> factory.build(this, obj)
                obj is GeometrySolid -> ThreeShapeFactory.build(this, obj)
                else -> error("Renderer for ${obj::class} not found")
            }
        }
    }

    private val canvasCache = HashMap<Element, ThreeCanvas>()

    public fun getOrCreateCanvas(
        element: Element,
        options: Canvas3DOptions = Canvas3DOptions(),
    ): ThreeCanvas = canvasCache.getOrPut(element) {
        ThreeCanvas(this, element, options)
    }

    override fun content(target: String): Map<Name, Any> {
        return when (target) {
            ElementVisionRenderer.TYPE -> mapOf("three".asName() to this)
            else -> super.content(target)
        }
    }

    override fun rateVision(vision: Vision): Int =
        if (vision is Solid) ElementVisionRenderer.DEFAULT_RATING else ElementVisionRenderer.ZERO_RATING

    internal fun renderSolid(
        element: Element,
        vision: Solid,
    ): ThreeCanvas = getOrCreateCanvas(element).apply {
        render(vision)
    }

    override fun render(element: Element, vision: Vision, meta: Meta) {
        renderSolid(
            element,
            vision as? Solid ?: error("Solid expected but ${vision::class} found"),
        ).apply {
            options.meta.update(meta)
        }
    }

    public companion object : PluginFactory<ThreePlugin> {
        override val tag: PluginTag = PluginTag("vision.threejs", PluginTag.DATAFORGE_GROUP)
        override val type: KClass<ThreePlugin> = ThreePlugin::class

        override fun build(context: Context, meta: Meta): ThreePlugin = ThreePlugin()
    }
}

public fun ThreePlugin.render(
    element: HTMLElement,
    obj: Solid,
    optionsBuilder: Canvas3DOptions.() -> Unit = {},
): ThreeCanvas = renderSolid(element, obj).apply {
    options.apply(optionsBuilder)
}

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