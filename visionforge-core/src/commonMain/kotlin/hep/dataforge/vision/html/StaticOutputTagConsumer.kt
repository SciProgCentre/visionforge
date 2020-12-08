package hep.dataforge.vision.html

import hep.dataforge.names.Name
import hep.dataforge.vision.Vision
import kotlinx.html.FlowContent
import kotlinx.html.TagConsumer
import kotlinx.html.stream.createHTML

public typealias HtmlVisionRenderer<V> = FlowContent.(V) -> Unit

/**
 * An [OutputTagConsumer] that directly renders given [Vision] using provided [renderer]
 */
public class StaticOutputTagConsumer<R, V : Vision>(
    root: TagConsumer<R>,
    prefix: String? = null,
    private val renderer: HtmlVisionRenderer<V>,
) : OutputTagConsumer<R, V>(root, prefix) {

    override fun FlowContent.renderVision(name: Name, vision: V): Unit = renderer(vision)
}

public fun <T : Any> HtmlVisionFragment<Vision>.renderToObject(
    root: TagConsumer<T>,
    prefix: String? = null,
    renderer: HtmlVisionRenderer<Vision>,
): T = StaticOutputTagConsumer(root, prefix, renderer).apply(content).finalize()

public fun HtmlVisionFragment<Vision>.renderToString(renderer: HtmlVisionRenderer<Vision>): String =
    renderToObject(createHTML(), null, renderer)