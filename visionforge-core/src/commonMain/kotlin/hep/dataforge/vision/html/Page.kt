package hep.dataforge.vision.html

import hep.dataforge.context.Context
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
            title(title)
        }
        body {
            embedVisionFragment(context.visionManager, fragment = content)
        }
    }.finalize()
}