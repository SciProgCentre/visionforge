package hep.dataforge.vision.html

import hep.dataforge.meta.Meta
import hep.dataforge.names.Name
import hep.dataforge.vision.Vision
import hep.dataforge.vision.VisionManager
import kotlinx.html.FlowContent
import kotlinx.html.TagConsumer
import kotlinx.html.script
import kotlinx.html.stream.createHTML
import kotlinx.html.unsafe

public typealias HtmlVisionRenderer<V> = FlowContent.(V, Meta) -> Unit

/**
 * An [OutputTagConsumer] that directly renders given [Vision] using provided [renderer]
 */
public class StaticOutputTagConsumer<R, V : Vision>(
    root: TagConsumer<R>,
    prefix: String? = null,
    private val renderer: HtmlVisionRenderer<V>,
) : OutputTagConsumer<R, V>(root, prefix) {
    override fun FlowContent.renderVision(name: Name, vision: V, outputMeta: Meta): Unit = renderer(vision, outputMeta)

    public companion object {
        public fun embed(manager: VisionManager): HtmlVisionRenderer<Vision> = { vision: Vision, _: Meta ->
            script {
                attributes["class"] = OUTPUT_DATA_CLASS
                unsafe {
                    +manager.encodeToString(vision)
                }
            }
        }
    }
}

public fun <T : Any> HtmlVisionFragment<Vision>.renderToObject(
    root: TagConsumer<T>,
    prefix: String? = null,
    renderer: HtmlVisionRenderer<Vision>,
): T = StaticOutputTagConsumer(root, prefix, renderer).apply(content).finalize()

/**
 * Render an object to HTML embedding the data as script bodies
 */
public fun <T : Any> HtmlVisionFragment<Vision>.embedToObject(
    manager: VisionManager,
    root: TagConsumer<T>,
    prefix: String? = null,
): T = renderToObject(root, prefix, StaticOutputTagConsumer.embed(manager))

public fun HtmlVisionFragment<Vision>.renderToString(renderer: HtmlVisionRenderer<Vision>): String =
    renderToObject(createHTML(), null, renderer)

/**
 * Convert a fragment to a string, embedding all visions data
 */
public fun HtmlVisionFragment<Vision>.embedToString(manager: VisionManager): String =
    embedToObject(manager, createHTML())

