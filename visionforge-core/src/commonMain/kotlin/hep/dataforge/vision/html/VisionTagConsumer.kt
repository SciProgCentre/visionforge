package hep.dataforge.vision.html

import hep.dataforge.meta.*
import hep.dataforge.names.Name
import hep.dataforge.names.toName
import hep.dataforge.vision.Vision
import hep.dataforge.vision.VisionManager
import kotlinx.html.*


/**
 * A placeholder object to attach inline vision builders.
 */
@DFExperimental
public class VisionOutput @PublishedApi internal constructor(){
    public var meta: Meta = Meta.EMPTY

    public inline fun meta(block: MetaBuilder.() -> Unit) {
        this.meta = Meta(block)
    }
}

/**
 * Modified  [TagConsumer] that allows rendering output fragments and visions in them
 */
public abstract class VisionTagConsumer<R>(
    private val root: TagConsumer<R>,
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
                    +VisionManager.defaultJson.encodeToString(MetaSerializer, outputMeta)
                }
            }
        }
        vision?.let { renderVision(name, it, outputMeta) }
    }

    @OptIn(DFExperimental::class)
    public inline fun <T> TagConsumer<T>.vision(
        name: Name,
        visionProvider: VisionOutput.() -> Vision,
    ): T {
        val output = VisionOutput()
        val vision = output.visionProvider()
        return vision(name, vision, output.meta)
    }

    @OptIn(DFExperimental::class)
    public inline fun <T> TagConsumer<T>.vision(
        name: String,
        visionProvider: VisionOutput.() -> Vision,
    ): T = vision(name.toName(), visionProvider)

    public inline fun <T> TagConsumer<T>.vision(
        vision: Vision,
    ): T = vision("vision[${vision.hashCode()}]".toName(), vision)

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
    }
}