package space.kscience.visionforge.markup

import kotlinx.html.TagConsumer
import kotlinx.html.div
import kotlinx.html.unsafe
import org.intellij.markdown.flavours.MarkdownFlavourDescriptor
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser

/**
 * Render markdown inside kotlinx-html tag
 */
public fun <T> TagConsumer<T>.markdown(
    flavour: MarkdownFlavourDescriptor = CommonMarkFlavourDescriptor(),
    block: () -> String
): T {
    val src = block()
    val parsedTree = MarkdownParser(flavour).buildMarkdownTreeFromString(src)
    return div("visionforge-markdown") {
        unsafe {
            +HtmlGenerator(src, parsedTree, flavour).generateHtml()
        }
    }
}