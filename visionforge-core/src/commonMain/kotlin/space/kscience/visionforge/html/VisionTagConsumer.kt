package space.kscience.visionforge.html

import kotlinx.html.*
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.PluginFactory
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.MetaSerializer
import space.kscience.dataforge.meta.MutableMeta
import space.kscience.dataforge.meta.isEmpty
import space.kscience.dataforge.misc.DFExperimental
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
@DFExperimental
@VisionDSL
public class VisionOutput @PublishedApi internal constructor(public val context: Context, public val name: Name?) {
    public var meta: Meta = Meta.EMPTY

    private val requirements: MutableSet<PluginFactory<*>> = HashSet()

    public fun requirePlugin(factory: PluginFactory<*>) {
        requirements.add(factory)
    }

    internal fun buildVisionManager(): VisionManager =
        if (requirements.all { req -> context.plugins.find(true) { it.tag == req.tag } != null }) {
            context.visionManager
        } else {
            val newContext = context.buildContext(NameToken(DEFAULT_VISION_NAME, name.toString()).asName()) {
                plugin(VisionManager)
                requirements.forEach { plugin(it) }
            }
            newContext.visionManager
        }

    public inline fun meta(block: MutableMeta.() -> Unit) {
        this.meta = Meta(block)
    }
}

/**
 * Modified  [TagConsumer] that allows rendering output fragments and visions in them
 */
@VisionDSL
@OptIn(DFExperimental::class)
public abstract class VisionTagConsumer<R>(
    private val root: TagConsumer<R>,
    public val context: Context,
    private val idPrefix: String? = null,
) : TagConsumer<R> by root {

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
    private fun <T> TagConsumer<T>.vision(
        name: Name,
        manager: VisionManager,
        vision: Vision,
        outputMeta: Meta = Meta.EMPTY,
    ): T = div {
        id = resolveId(name)
        classes = setOf(OUTPUT_CLASS)
        vision.setAsRoot(manager)
        attributes[OUTPUT_NAME_ATTRIBUTE] = name.toString()
        if (!outputMeta.isEmpty()) {
            //Hard-code output configuration
            script {
                attributes["class"] = OUTPUT_META_CLASS
                unsafe {
                    +manager.jsonFormat.encodeToString(MetaSerializer, outputMeta)
                }
            }
        }
        renderVision(manager, name, vision, outputMeta)
    }

    /**
     * Insert a vision in this HTML.
     * TODO replace by multi-receiver
     */
    @OptIn(DFExperimental::class)
    public fun <T> TagConsumer<T>.vision(
        name: Name? = null,
        @OptIn(DFExperimental::class) visionProvider: VisionOutput.() -> Vision,
    ): T {
        val output = VisionOutput(context, name)
        val vision = output.visionProvider()
        val actualName = name ?: NameToken(DEFAULT_VISION_NAME, vision.hashCode().toUInt().toString()).asName()
        return vision(actualName, output.buildVisionManager(), vision, output.meta)
    }

    /**
     * TODO to be replaced by multi-receiver
     */
    @OptIn(DFExperimental::class)
    @VisionDSL
    public fun <T> TagConsumer<T>.vision(
        name: String?,
        @OptIn(DFExperimental::class) visionProvider: VisionOutput.() -> Vision,
    ): T = vision(name?.parseAsName(), visionProvider)

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