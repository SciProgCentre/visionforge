package space.kscience.visionforge.html

import kotlinx.html.*
import space.kscience.dataforge.context.Context

public data class Page(
    public val context: Context,
    public val headers: Map<String, HtmlFragment> = emptyMap(),
    public val content: HtmlVisionFragment,
) {
    public fun <R> render(root: TagConsumer<R>): R = root.apply {
        head {
            meta {
                charset = "utf-8"
            }
            headers.values.forEach {
                fragment(it)
            }
        }
        body {
            visionFragment(context, fragment = content)
        }
    }.finalize()

    public companion object{
        /**
         * Use a script with given [src] as a global header for all pages.
         */
        public fun scriptHeader(src: String, block: SCRIPT.() -> Unit = {}): HtmlFragment = {
            script {
                type = "text/javascript"
                this.src = src
                block()
            }
        }

        /**
         * Use css with given stylesheet link as a global header for all pages.
         */
        public fun styleSheetHeader(href: String, block: LINK.() -> Unit = {}): HtmlFragment = {
            link {
                rel = "stylesheet"
                this.href = href
                block()
            }
        }

        public fun title(title:String): HtmlFragment = {
            title(title)
        }
    }
}