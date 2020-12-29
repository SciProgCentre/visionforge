package hep.dataforge.vision

import hep.dataforge.context.Context
import hep.dataforge.meta.DFExperimental
import hep.dataforge.vision.html.HtmlFragment
import hep.dataforge.vision.html.HtmlVisionFragment
import hep.dataforge.vision.html.embedVisionFragment
import hep.dataforge.vision.html.fragment
import kotlinx.html.body
import kotlinx.html.head
import kotlinx.html.meta
import kotlinx.html.stream.createHTML
import kotlinx.html.title
import java.awt.Desktop
import java.nio.file.Files
import java.nio.file.Path


/**
 * Make a file with the embedded vision data
 */
@DFExperimental
public fun Context.makeVisionFile(
    fragment: HtmlVisionFragment,
    path: Path? = null,
    title: String = "VisionForge page",
    show: Boolean = true,
    headerBuilder: (Path) -> HtmlFragment,
) {
    val actualFile = path?.let {
        Path.of(System.getProperty("user.home")).resolve(path)
    } ?: Files.createTempFile("tempPlot", ".html")
    //Files.createDirectories(actualFile.parent)
    val htmlString = createHTML().apply {
        head {
            meta {
                charset = "utf-8"
                fragment(headerBuilder(actualFile))
            }
            title(title)
        }
        body {
            embedVisionFragment(visionManager, fragment = fragment)
        }
    }.finalize()

    Files.writeString(actualFile, htmlString)
    if (show) {
        Desktop.getDesktop().browse(actualFile.toFile().toURI())
    }
}