package hep.dataforge.vision.solid.three

import hep.dataforge.context.*
import hep.dataforge.meta.Meta
import hep.dataforge.misc.DFExperimental
import hep.dataforge.names.*
import hep.dataforge.vision.*
import hep.dataforge.vision.solid.*
import hep.dataforge.vision.solid.specifications.Canvas3DOptions
import info.laht.threekt.core.Object3D
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import kotlin.collections.set
import kotlin.reflect.KClass
import info.laht.threekt.objects.Group as ThreeGroup

public class ThreePlugin : AbstractPlugin(), ElementVisionRenderer {
    override val tag: PluginTag get() = Companion.tag

    public val solids: Solids by require(Solids)

    private val objectFactories = HashMap<KClass<out Solid>, ThreeFactory<*>>()
    private val compositeFactory = ThreeCompositeFactory(this)

    //TODO generate a separate supervisor update scope
    internal val updateScope: CoroutineScope get() = context

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

    public fun buildObject3D(obj: Solid): Object3D = when (obj) {
        is ThreeVision -> obj.render(this)
        is SolidReferenceGroup -> ThreeReferenceFactory(this, obj)
        is SolidGroup -> {
            val group = ThreeGroup()
            obj.children.forEach { (token, child) ->
                if (child is Solid && token != SolidGroup.PROTOTYPES_TOKEN && child.ignore != true) {
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

                obj.onPropertyChange(updateScope) { name ->
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
                }

                obj.structureChanges.onEach { (nameToken, _, child) ->
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
                }.launchIn(updateScope)
            }
        }
        is Composite -> compositeFactory(this, obj)
        else -> {
            //find specialized factory for this type if it is present
            val factory: ThreeFactory<Solid>? = findObjectFactory(obj::class)
            when {
                factory != null -> factory(this, obj)
                obj is GeometrySolid -> ThreeShapeFactory(this, obj)
                else -> error("Renderer for ${obj::class} not found")
            }
        }
    }

    public fun createCanvas(
        element: Element,
        options: Canvas3DOptions = Canvas3DOptions.empty(),
    ): ThreeCanvas = ThreeCanvas(this, options).apply {
        attach(element)
    }

    override fun content(target: String): Map<Name, Any> {
        return when (target) {
            ElementVisionRenderer.TYPE -> mapOf("three".asName() to this)
            else -> super.content(target)
        }
    }

    override fun rateVision(vision: Vision): Int =
        if (vision is Solid) ElementVisionRenderer.DEFAULT_RATING else ElementVisionRenderer.ZERO_RATING

    public fun renderSolid(
        element: Element,
        vision: Solid,
        options: Canvas3DOptions,
    ): ThreeCanvas = createCanvas(element, options).apply {
        render(vision)
    }

    override fun render(element: Element, vision: Vision, meta: Meta) {
        renderSolid(
            element,
            vision as? Solid ?: error("Solid expected but ${vision::class} found"),
            Canvas3DOptions.read(meta)
        )
    }

    public companion object : PluginFactory<ThreePlugin> {
        override val tag: PluginTag = PluginTag("vision.threejs", PluginTag.DATAFORGE_GROUP)
        override val type: KClass<ThreePlugin> = ThreePlugin::class
        override fun invoke(meta: Meta, context: Context): ThreePlugin = ThreePlugin()
    }
}

/**
 * Ensure that [ThreePlugin] is loaded in the global [VisionForge] context
 */
@DFExperimental
public fun VisionForge.useThreeJs() {
    plugins.fetch(ThreePlugin)
}

public fun ThreePlugin.render(
    element: HTMLElement,
    obj: Solid,
    options: Canvas3DOptions.() -> Unit = {},
): ThreeCanvas = renderSolid(element, obj, Canvas3DOptions(options))

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

public fun Context.withThreeJs(): Context = apply {
    plugins.fetch(ThreePlugin)
}