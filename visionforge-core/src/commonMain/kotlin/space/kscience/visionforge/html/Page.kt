package space.kscience.visionforge.html

import kotlinx.html.*
import space.kscience.dataforge.misc.DFExperimental
import space.kscience.visionforge.VisionManager

//data class HeaderContainer

public data class Page(
    public val visionManager: VisionManager,
    public val title: String,
    public val headers: Map<String, HtmlFragment>,
    public val content: HtmlVisionFragment,
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
            visionFragment(visionManager, fragment = content)
        }
    }.finalize()
}


@DFExperimental
public fun VisionManager.page(
    title: String = "VisionForge page",
    vararg headers: Pair<String, HtmlFragment>,
    content: HtmlVisionFragment,
): Page = Page(this, title, mapOf(*headers), content)