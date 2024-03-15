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
import space.kscience.visionforge.html.VisionTagConsumer.Companion.DEFAULT_VISION_NAME
import space.kscience.visionforge.setAsRoot
import space.kscience.visionforge.visionManager
import kotlin.collections.set

@DslMarker
public annotation class VisionDSL

/**
 * A placeholder object to attach inline vision builders.
 */
@VisionDSL
public class VisionOutput(override val context: Context, public val name: Name): ContextAware {
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
 * Modified  [TagConsumer] that allows rendering output fragments and visions in them
 */
@VisionDSL
public abstract class VisionTagConsumer<R>(
    private val root: TagConsumer<R>,
    public val visionManager: VisionManager,
    private val idPrefix: String? = null,
) : TagConsumer<R> by root, ContextAware {

    override val context: Context get() = visionManager.context

    public open fun resolveId(name: Name): String = (idPrefix ?: "output") + "[$name]"

    /**
     * Render a vision inside the output fragment
     * @param manager a [VisionManager] to be used in renderer
     * @param name name of the output container
     * @param vision an object to be rendered
     * @param outputMeta optional configuration for the output container
     */
    protected abstract fun DIV.renderVision(manager: VisionManager, name: Name, vision: Vision, outputMeta: Meta)

    /**
     * Create a placeholder for a vision output with optional [Vision] in it
     * TODO with multi-receivers could be replaced by [VisionTagConsumer, TagConsumer] extension
     */
    protected fun <T> TagConsumer<T>.addVision(
        name: Name,
        manager: VisionManager,
        vision: Vision?,
        outputMeta: Meta = Meta.EMPTY,
    ): T = if (vision == null) div {
        +"Empty Vision output"
    } else div {
        id = resolveId(name)
        classes = setOf(OUTPUT_CLASS)
        if (vision.parent == null) {
            vision.setAsRoot(manager)
        }
        attributes[OUTPUT_NAME_ATTRIBUTE] = name.toString()
        renderVision(manager, name, vision, outputMeta)
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

    /**
     * Insert a vision in this HTML.
     * TODO replace by multi-receiver
     */
    @VisionDSL
    public open fun <T> TagConsumer<T>.vision(
        name: Name? = null,
        buildOutput: VisionOutput.() -> Vision,
    ): T {
        val actualName = name ?: NameToken(DEFAULT_VISION_NAME, buildOutput.hashCode().toUInt().toString()).asName()
        val output = VisionOutput(context, actualName)
        val vision = output.buildOutput()
        return addVision(actualName, output.visionManager, vision, output.meta)
    }

    /**
     * TODO to be replaced by multi-receiver
     */
    @VisionDSL
    public fun <T> TagConsumer<T>.vision(
        name: String?,
        buildOutput: VisionOutput.() -> Vision,
    ): T = vision(name?.parseAsName(), buildOutput)

    @VisionDSL
    public open fun <T> TagConsumer<T>.vision(
        vision: Vision,
        name: Name? = null,
        outputMeta: Meta = Meta.EMPTY,
    ) {
        val actualName = name ?: NameToken(DEFAULT_VISION_NAME, vision.hashCode().toUInt().toString()).asName()
        addVision(actualName, context.visionManager, vision, outputMeta)
    }

    /**
     * Process the resulting object produced by [TagConsumer]
     */
    protected open fun processResult(result: R) {
        //do nothing by default
    }

    override fun finalize(): R = root.finalize().also { processResult(it) }

    public companion object {
        public const val OUTPUT_CLASS: String = "visionforge-output"
        public const val OUTPUT_META_CLASS: String = "visionforge-output-meta"
        public const val OUTPUT_DATA_CLASS: String = "visionforge-output-data"

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