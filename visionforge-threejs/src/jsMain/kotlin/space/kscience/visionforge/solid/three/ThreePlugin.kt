package space.kscience.visionforge.solid.three

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.jetbrains.compose.web.dom.DOMScope
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import space.kscience.dataforge.context.*
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.names.*
import space.kscience.visionforge.*
import space.kscience.visionforge.compose.ComposeVisionRenderer
import space.kscience.visionforge.solid.*
import space.kscience.visionforge.solid.specifications.Canvas3DOptions
import space.kscience.visionforge.solid.three.compose.ThreeView
import space.kscience.visionforge.solid.three.set
import three.core.Object3D
import kotlin.collections.set
import kotlin.reflect.KClass
import three.objects.Group as ThreeGroup

/**
 * A plugin that handles Three Object3D representation of Visions.
 */
public class ThreePlugin : AbstractPlugin(), ComposeVisionRenderer {
    override val tag: PluginTag get() = Companion.tag

    public val solids: Solids by require(Solids)

    public val client: VisionClient by require(JsVisionClient)

    private val objectFactories = HashMap<KClass<out Solid>, ThreeFactory<*>>()
    private val compositeFactory = ThreeCompositeFactory(this)

//    internal val updateScope: CoroutineScope get() = context

    init {
        //Add specialized factories here
        objectFactories[Box::class] = ThreeBoxFactory
        objectFactories[Convex::class] = ThreeConvexFactory
        objectFactories[Sphere::class] = ThreeSphereFactory
        objectFactories[PolyLine::class] = ThreeSmartLineFactory
        objectFactories[SolidLabel::class] = ThreeCanvasLabelFactory
        objectFactories[AmbientLightSource::class] = ThreeAmbientLightFactory
        objectFactories[PointLightSource::class] = ThreePointLightFactory
        objectFactories[StlUrlSolid::class] = ThreeStlFactory
        objectFactories[StlBinarySolid::class] = ThreeStlFactory
        objectFactories[AxesSolid::class] = ThreeAxesFactory
    }

    @Suppress("UNCHECKED_CAST")
    private fun findObjectFactory(type: KClass<out Vision>): ThreeFactory<Solid>? =
        (objectFactories[type] ?: context.gather<ThreeFactory<*>>(ThreeFactory.TYPE).values.find { it.type == type })
                as ThreeFactory<Solid>?

    /**
     * Build an Object3D representation of the given [Solid].
     *
     * @param vision [Solid] object to build a representation of;
     * @param observe whether the constructed Object3D should be changed when the
     *  original [Vision] changes.
     */
    public suspend fun buildObject3D(vision: Solid, observe: Boolean = true): Object3D = when (vision) {
        is ThreeJsVision -> vision.render(this)
        is SolidReference -> ThreeReferenceFactory.build(this, vision, observe)
        is SolidGroup -> {
            val group = ThreeGroup()
            vision.items.forEach { (token, child) ->
                if (token != SolidGroup.PROTOTYPES_TOKEN && child.ignore != true) {
                    try {
                        val object3D = buildObject3D(
                            child,
                            if (token.body == VisionChildren.STATIC_TOKEN_BODY) false else observe
                        )
                        // disable tracking changes for statics
                        group[token] = object3D
                    } catch (ex: Throwable) {
                        logger.error(ex) { "Failed to render vision with token $token and type ${child::class}" }
                    }
                }
            }

            group.apply {
                updatePosition(vision)
                //obj.onChildrenChange()
                if (observe) {
                    vision.properties.changes.onEach { name ->
                        if (
                            name.startsWith(Solid.POSITION_KEY) ||
                            name.startsWith(Solid.ROTATION_KEY) ||
                            name.startsWith(Solid.SCALE_KEY)
                        ) {
                            //update position of mesh using this object
                            updatePosition(vision)
                        } else if (name == Vision.VISIBLE_KEY) {
                            visible = vision.visible ?: true
                        }
                    }.launchIn(context)

                    vision.children.changes.onEach { childName ->
                        if (childName.isEmpty()) return@onEach

                        val child = vision.children.getChild(childName)

                        logger.debug { "Changing vision $childName to $child" }

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
                                logger.error(ex) { "Failed to render vision with name $childName" }
                            }
                        }
                    }.launchIn(context)
                }
            }
        }

        is Composite -> compositeFactory.build(this, vision, observe)
        else -> {
            //find a specialized factory for this type if it is present
            val factory: ThreeFactory<Solid>? = findObjectFactory(vision::class)
            when {
                factory != null -> factory.build(this, vision, observe)
                vision is GeometrySolid -> ThreeShapeFactory.build(this, vision, observe)
                else -> error("Renderer for ${vision::class} not found")
            }
        }
    }

    private val canvasCache = HashMap<Element, ThreeCanvas>()

    /**
     * Return a [ThreeCanvas] object attached to the given [Element].
     * If there is no canvas bound, a new canvas object is created
     * and returned.
     *
     * @param element HTML element to which the canvas is
     *  (or should be if it is created by this call) attached;
     * @param options canvas options that are applied to a newly
     *  created [ThreeCanvas] in case it does not exist.
     */
    public fun getOrCreateCanvas(
        element: Element,
        options: Canvas3DOptions,
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

    /**
     * Render the given [Solid] Vision in a [ThreeCanvas] attached
     * to the [element]. Canvas objects are cached, so subsequent calls
     * with the same [element] value do not create new canvas objects,
     * but they replace existing content, so multiple Visions cannot be
     * displayed in a single [ThreeCanvas].
     *
     * @param element HTML element [ThreeCanvas] should be
     *  attached to;
     *  @param vision Vision to render;
     *  @param options options that are applied to a canvas
     *    in case it is not in the cache and should be created.
     */
    internal fun renderSolid(
        element: Element,
        vision: Solid,
        options: Canvas3DOptions,
    ): ThreeCanvas = getOrCreateCanvas(element, options).apply {
        render(vision)
    }

    @Composable
    override fun DOMScope<Element>.render(client: VisionClient, name: Name, vision: Vision, meta: Meta) {
        require(vision is Solid) { "Expected Solid but found ${vision::class}" }
        ThreeView(context, vision, null, Canvas3DOptions.read(meta))
    }

    public companion object : PluginFactory<ThreePlugin> {
        override val tag: PluginTag = PluginTag("vision.threejs", PluginTag.DATAFORGE_GROUP)

        override fun build(context: Context, meta: Meta): ThreePlugin = ThreePlugin()
    }
}

/**
 * Render the given [Solid] Vision in a [ThreeCanvas] attached
 * to the [element]. Canvas objects are cached, so subsequent calls
 * with the same [element] value do not create new canvas objects,
 * but they replace existing content, so multiple Visions cannot be
 * displayed in a single [ThreeCanvas].
 *
 * @param element HTML element [ThreeCanvas] should be
 *  attached to;
 *  @param obj Vision to render;
 *  @param optionsBuilder option builder that is applied to a canvas
 *    in case it is not in the cache and should be created.
 */
public fun ThreePlugin.render(
    element: HTMLElement,
    obj: Solid,
    optionsBuilder: Canvas3DOptions.() -> Unit = {},
): ThreeCanvas = renderSolid(element, obj, Canvas3DOptions(optionsBuilder)).apply {
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
            children.find { it.name == token.toString() } ?: ThreeGroup().also { group ->
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
