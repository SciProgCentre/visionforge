package space.kscience.visionforge.html

import kotlinx.html.*
import space.kscience.visionforge.VisionManager

/**
 * A structure representing a single page with Visions to be rendered.
 *
 * @param pageHeaders static headers for this page.
 */
public data class VisionPage(
    public val visionManager: VisionManager,
    public val pageHeaders: Map<String, HtmlFragment> = emptyMap(),
    public val content: HtmlVisionFragment,
) {
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