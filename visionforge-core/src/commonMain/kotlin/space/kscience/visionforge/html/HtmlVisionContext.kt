package space.kscience.visionforge.html

import kotlinx.html.*
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.ContextAware
import space.kscience.dataforge.context.PluginFactory
import space.kscience.dataforge.meta.*
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.NameToken
import space.kscience.dataforge.names.asName
import space.kscience.dataforge.names.parseAsName
import space.kscience.visionforge.Vision
import space.kscience.visionforge.VisionManager
import space.kscience.visionforge.html.HtmlVisionContext.Companion.DEFAULT_VISION_NAME
import space.kscience.visionforge.html.HtmlVisionContext.Companion.OUTPUT_CLASS
import space.kscience.visionforge.html.HtmlVisionContext.Companion.OUTPUT_DIV_CLASSES_KEY
import space.kscience.visionforge.html.HtmlVisionContext.Companion.OUTPUT_META_CLASS
import space.kscience.visionforge.html.HtmlVisionContext.Companion.OUTPUT_NAME_ATTRIBUTE
import space.kscience.visionforge.setAsRoot
import space.kscience.visionforge.visionManager

@DslMarker
public annotation class VisionDSL

/**
 * A placeholder object to attach inline vision builders.
 */
@VisionDSL
public class VisionOutput(override val context: Context, public val name: Name) : ContextAware {
    public var meta: Meta = Meta.EMPTY

    private val requirements: MutableSet<PluginFactory<*>> = HashSet()

    public fun requirePlugin(factory: PluginFactory<*>) {
        requirements.add(factory)
    }

    public val visionManager: VisionManager
        get() = if (requirements.all { req -> context.plugins.find(true) { it.tag == req.tag } != null }) {
            context.visionManager
        } else {
            val newContext = context.buildContext(NameToken(DEFAULT_VISION_NAME, name.toString()).asName()) {
                plugin(VisionManager)
                requirements.forEach { plugin(it) }
            }
            newContext.visionManager
        }

}

public inline fun VisionOutput.meta(block: MutableMeta.() -> Unit) {
    this.meta = Meta(block)
}

public fun VisionOutput.meta(metaRepr: MetaRepr) {
    this.meta = metaRepr.toMeta()
}

/**
 * Modified scope that allows rendering output fragments and visions in them
 */
@VisionDSL
public abstract class HtmlVisionContext(
    override val context: Context,
    private val idPrefix: String? = null,
) : ContextAware {

    public open fun resolveId(name: Name): String = (idPrefix ?: "output") + "[$name]"

    /**
     * Render a vision inside the output fragment
     * @param manager a [VisionManager] to be used in renderer
     * @param name name of the output container
     * @param vision an object to be rendered
     * @param outputMeta optional configuration for the output container
     */
    public abstract fun renderVision(div: DIV, manager: VisionManager, name: Name, vision: Vision, outputMeta: Meta)


    public companion object {
        public const val OUTPUT_CLASS: String = "visionforge-output"
        public const val OUTPUT_META_CLASS: String = "visionforge-output-meta"
        public const val OUTPUT_DATA_CLASS: String = "visionforge-output-data"

        public const val OUTPUT_DIV_CLASSES_KEY: String = "classes"

        public const val OUTPUT_FETCH_ATTRIBUTE: String = "data-output-fetch"
        public const val OUTPUT_CONNECT_ATTRIBUTE: String = "data-output-connect"

        public const val OUTPUT_RENDERED: String = "data-output-rendered"

        public const val OUTPUT_NAME_ATTRIBUTE: String = "data-output-name"
        public const val OUTPUT_ENDPOINT_ATTRIBUTE: String = "data-output-endpoint"
        public const val DEFAULT_ENDPOINT: String = "."

        public const val AUTO_DATA_ATTRIBUTE: String = "@auto"

        public const val DEFAULT_VISION_NAME: String = "vision"
    }
}

/**
 * Create a placeholder for a vision output with optional [Vision] in it
 */
context(htmlContext: HtmlVisionContext)
public fun <T> TagConsumer<T>.addVision(
    name: Name,
    manager: VisionManager,
    vision: Vision?,
    outputMeta: Meta = Meta.EMPTY,
): T = if (vision == null) div {
    +"Empty Vision output"
} else div {
    id = htmlContext.resolveId(name)

    classes = setOf(OUTPUT_CLASS, *(outputMeta[OUTPUT_DIV_CLASSES_KEY].stringList?.toTypedArray() ?: emptyArray()))
    if (vision.parent == null) {
        vision.setAsRoot(manager)
    }
    attributes[OUTPUT_NAME_ATTRIBUTE] = name.toString()
    htmlContext.renderVision(this, manager, name, vision, outputMeta)
    if (!outputMeta.isEmpty()) {
        //Hard-code output configuration
        script {
            type = "text/json"
            attributes["class"] = OUTPUT_META_CLASS
            unsafe {
                +("\n" + manager.jsonFormat.encodeToString(MetaSerializer, outputMeta) + "\n")
            }
        }
    }
}


@VisionDSL
context(htmlContext: HtmlVisionContext)
public fun <T> TagConsumer<T>.vision(
    vision: Vision,
    name: Name? = null,
    outputMeta: Meta = Meta.EMPTY,
) {
    val actualName = name ?: NameToken(DEFAULT_VISION_NAME, vision.hashCode().toUInt().toString()).asName()
    addVision(actualName, htmlContext.context.visionManager, vision, outputMeta)
}


context(htmlContext: HtmlVisionContext)
private fun <T> TagConsumer<T>.vision(
    visionManager: VisionManager,
    name: Name,
    vision: Vision,
    outputMeta: Meta = Meta.EMPTY,
): T = div {
    id = htmlContext.resolveId(name)
    classes = setOf(OUTPUT_CLASS)
    vision.setAsRoot(visionManager)
    attributes[OUTPUT_NAME_ATTRIBUTE] = name.toString()
    if (!outputMeta.isEmpty()) {
        //Hard-code output configuration
        script {
            attributes["class"] = OUTPUT_META_CLASS
            unsafe {
                +visionManager.jsonFormat.encodeToString(MetaSerializer, outputMeta)
            }
        }
    }
    htmlContext.renderVision(this, visionManager, name, vision, outputMeta)
}

context(htmlContext: HtmlVisionContext)
private fun <T> TagConsumer<T>.vision(
    name: Name,
    vision: Vision,
    outputMeta: Meta = Meta.EMPTY,
): T = vision(htmlContext.context.visionManager, name, vision, outputMeta)

/**
 * Insert a vision in this HTML.
 */
@VisionDSL
context(htmlContext: HtmlVisionContext)
public fun <T> TagConsumer<T>.vision(
    name: Name? = null,
    visionProvider: VisionOutput.() -> Vision,
): T {
    val actualName = name ?: NameToken(DEFAULT_VISION_NAME, visionProvider.hashCode().toUInt().toString()).asName()
    val output = VisionOutput(htmlContext.context, actualName)
    val vision = output.visionProvider()
    return vision(output.visionManager, actualName, vision, output.meta)
}

/**
 * Insert a vision in this HTML.
 */
@VisionDSL
context(htmlContext: HtmlVisionContext)
public fun <T> TagConsumer<T>.vision(
    name: String?,
    visionProvider: VisionOutput.() -> Vision,
): T = vision(name?.parseAsName(), visionProvider)