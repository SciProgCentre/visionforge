package space.kscience.visionforge.html

import kotlinx.html.*
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.MetaSerializer
import space.kscience.dataforge.meta.MutableMeta
import space.kscience.dataforge.meta.isEmpty
import space.kscience.dataforge.misc.DFExperimental
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.NameToken
import space.kscience.dataforge.names.asName
import space.kscience.visionforge.Vision
import space.kscience.visionforge.VisionManager
import kotlin.collections.set

@DslMarker
public annotation class VisionDSL

/**
 * A placeholder object to attach inline vision builders.
 */
@DFExperimental
@VisionDSL
public class VisionOutput @PublishedApi internal constructor(public val manager: VisionManager) {
    public var meta: Meta = Meta.EMPTY

    //TODO expose a way to define required plugins.

    public inline fun meta(block: MutableMeta.() -> Unit) {
        this.meta = Meta(block)
    }
}

/**
 * Modified  [TagConsumer] that allows rendering output fragments and visions in them
 */
@VisionDSL
public abstract class VisionTagConsumer<R>(
    private val root: TagConsumer<R>,
    public val manager: VisionManager,
    private val idPrefix: String? = null,
) : TagConsumer<R> by root {

    public open fun resolveId(name: Name): String = (idPrefix ?: "output:") + name.toString()

    /**
     * Render a vision inside the output fragment
     * @param name name of the output container
     * @param vision an object to be rendered
     * @param outputMeta optional configuration for the output container
     */
    protected abstract fun DIV.renderVision(name: Name, vision: Vision, outputMeta: Meta)

    /**
     * Create a placeholder for a vision output with optional [Vision] in it
     * TODO with multi-receivers could be replaced by [VisionTagConsumer, TagConsumer] extension
     */
    public fun <T> TagConsumer<T>.vision(
        name: Name,
        vision: Vision? = null,
        outputMeta: Meta = Meta.EMPTY,
    ): T = div {
        id = resolveId(name)
        classes = setOf(OUTPUT_CLASS)
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
        vision?.let {
            renderVision(name, it, outputMeta)
        }
    }

    @OptIn(DFExperimental::class)
    public inline fun <T> TagConsumer<T>.vision(
        name: Name,
        visionProvider: VisionOutput.() -> Vision,
    ): T {
        val output = VisionOutput(manager)
        val vision = output.visionProvider()
        return vision(name, vision, output.meta)
    }

    /**
     * TODO to be replaced by multi-receiver
     */
    @OptIn(DFExperimental::class)
    @VisionDSL
    public inline fun <T> TagConsumer<T>.vision(
        name: String = DEFAULT_VISION_NAME,
        visionProvider: VisionOutput.() -> Vision,
    ): T = vision(Name.parse(name), visionProvider)

    public fun <T> TagConsumer<T>.vision(
        vision: Vision,
    ): T = vision(NameToken("vision", vision.hashCode().toString()).asName(), vision)

    /**
     * Process the resulting object produced by [TagConsumer]
     */
    protected open fun processResult(result: R) {
        //do nothing by default
    }

    override fun finalize(): R {
        return root.finalize().also { processResult(it) }
    }

    public companion object {
        public const val OUTPUT_CLASS: String = "visionforge-output"
        public const val OUTPUT_META_CLASS: String = "visionforge-output-meta"
        public const val OUTPUT_DATA_CLASS: String = "visionforge-output-data"

        public const val OUTPUT_FETCH_ATTRIBUTE: String = "data-output-fetch"
        public const val OUTPUT_CONNECT_ATTRIBUTE: String = "data-output-connect"

        public const val OUTPUT_NAME_ATTRIBUTE: String = "data-output-name"
        public const val OUTPUT_ENDPOINT_ATTRIBUTE: String = "data-output-endpoint"
        public const val DEFAULT_ENDPOINT: String = "."

        public const val DEFAULT_VISION_NAME: String = "vision"
    }
}