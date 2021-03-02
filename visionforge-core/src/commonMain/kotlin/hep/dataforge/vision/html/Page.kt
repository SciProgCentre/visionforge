package hep.dataforge.vision.html

import hep.dataforge.context.Context
import hep.dataforge.misc.DFExperimental
import hep.dataforge.vision.VisionForge
import hep.dataforge.vision.context
import hep.dataforge.vision.visionManager
import kotlinx.html.*

public data class Page(
    public val context: Context,
    public val title: String,
    public val headers: Map<String, HtmlFragment>,
    public val content: HtmlVisionFragment
) {
    public fun <R> render(root: TagConsumer<R>): R = root.apply {
        head {
            meta {
                charset = "utf-8"
                headers.values.forEach {
                    fragment(it)
                }
            }
            title(this@Page.title)
        }
        body {
            embedVisionFragment(context.visionManager, fragment = content)
        }
    }.finalize()
}


@DFExperimental
public fun VisionForge.page(
    title: String = "VisionForge page",
    vararg headers: Pair<String, HtmlFragment>,
    content: HtmlVisionFragment,
): Page = Page(context, title, mapOf(*headers), content)