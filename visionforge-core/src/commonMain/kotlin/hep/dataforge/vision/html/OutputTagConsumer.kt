package hep.dataforge.vision.html

import hep.dataforge.names.Name
import hep.dataforge.names.toName
import hep.dataforge.vision.Vision
import kotlinx.html.*

/**
 * An HTML div wrapper that includes the output [name] and inherited [render] function
 */
public class OutputDiv<in V : Vision>(
    private val div: DIV,
    public val name: Name,
    public val render: (V) -> Unit,
) : HtmlBlockTag by div

/**
 * Modified  [TagConsumer] that allows rendering output fragments and visions in them
 */
public abstract class OutputTagConsumer<R, V : Vision>(
    private val root: TagConsumer<R>,
    private val idPrefix: String? = null,
) : TagConsumer<R> by root {

    public open fun resolveId(name: Name): String = (idPrefix ?: "output:") + name.toString()

    /**
     * Render a vision inside the output fragment
     */
    protected abstract fun FlowContent.renderVision(name: Name, vision: V)

    /**
     * Create a placeholder for an output window
     */
    public fun <T> TagConsumer<T>.visionOutput(
        name: Name,
        block: OutputDiv<V>.() -> Unit = {},
    ): T = div {
        id = resolveId(name)
        classes = setOf(OUTPUT_CLASS)
        attributes[OUTPUT_NAME_ATTRIBUTE] = name.toString()
        OutputDiv<V>(this, name) { renderVision(name, it) }.block()
    }

    public fun <T> TagConsumer<T>.visionOutput(
        name: String,
        block: OutputDiv<V>.() -> Unit = {},
    ): T = visionOutput(name.toName(), block)


    public fun <T> TagConsumer<T>.vision(name: Name, vision: V): Unit {
        visionOutput(name) {
            render(vision)
        }
    }

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
        public const val OUTPUT_NAME_ATTRIBUTE: String = "data-output-name"
        public const val OUTPUT_ENDPOINT_ATTRIBUTE: String = "data-output-endpoint"
        public const val DEFAULT_ENDPOINT: String = "."
    }
}