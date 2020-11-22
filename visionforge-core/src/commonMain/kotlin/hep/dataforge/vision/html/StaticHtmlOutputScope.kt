package hep.dataforge.vision.html

import hep.dataforge.vision.Vision
import kotlinx.html.FlowContent
import kotlinx.html.TagConsumer
import kotlinx.html.stream.createHTML

public typealias HtmlVisionRenderer<V> = FlowContent.(V) -> Unit

public class StaticHtmlOutputScope<R, V : Vision>(
    root: TagConsumer<R>,
    prefix: String? = null,
    private val render: HtmlVisionRenderer<V>,
) : HtmlOutputScope<R, V>(root, prefix) {

    override fun renderVision(htmlOutput: HtmlOutput<V>, vision: V) {
        htmlOutput.div.render(vision)
    }
}

public fun <T : Any> HtmlVisionFragment<Vision>.renderToObject(
    root: TagConsumer<T>,
    prefix: String? = null,
    renderer: HtmlVisionRenderer<Vision>,
): T = StaticHtmlOutputScope(root, prefix, renderer).apply(content).finalize()

public fun HtmlVisionFragment<Vision>.renderToString(renderer: HtmlVisionRenderer<Vision>): String =
    renderToObject(createHTML(), null, renderer)