package hep.dataforge.vision.html

import hep.dataforge.names.Name
import hep.dataforge.names.toName
import hep.dataforge.vision.Vision
import kotlinx.html.DIV
import kotlinx.html.TagConsumer
import kotlinx.html.div
import kotlinx.html.id

public class HtmlOutput<V : Vision>(
    public val outputScope: HtmlOutputScope<*, V>,
    public val name: Name,
    public val div: DIV,
)

public abstract class HtmlOutputScope<R, V : Vision>(
    private val root: TagConsumer<R>,
    public val prefix: String? = null,
) : TagConsumer<R> by root {

    public open fun resolveId(name: Name): String = (prefix ?: "output:") + name.toString()

    /**
     * Create a placeholder but do not attach any [Vision] to it
     */
    public inline fun <T> TagConsumer<T>.visionOutput(
        name: Name,
        crossinline block: HtmlOutput<V>.() -> Unit = {},
    ): T = div {
        this.id = resolveId(name)
        @Suppress("UNCHECKED_CAST")
        HtmlOutput(this@HtmlOutputScope, name, this).block()
    }


    public inline fun <T> TagConsumer<T>.visionOutput(
        name: String,
        crossinline block: HtmlOutput<V>.() -> Unit = {},
    ): T = visionOutput(name.toName(), block)

    /**
     * Create a placeholder and put a [Vision] in it
     */
    public abstract fun renderVision(htmlOutput: HtmlOutput<V>, vision: V)

    public fun <T> TagConsumer<T>.vision(name: Name, vision: V): Unit {
        visionOutput(name) {
            renderVision(this, vision)
        }
    }
}