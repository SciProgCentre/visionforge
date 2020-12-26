package hep.dataforge.vision

import hep.dataforge.meta.DFExperimental
import hep.dataforge.vision.html.*
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import java.awt.Desktop
import java.nio.file.Files
import java.nio.file.Path


/**
 * Make a file with the embedded vision data
 */
@DFExperimental
public fun HtmlVisionFragment.makeFile(
    manager: VisionManager,
    vararg headers: HtmlFragment,
    path: Path? = null,
    title: String = "VisionForge page",
    show: Boolean = true,
) {
    val actualFile = path?.let {
        Path.of(System.getProperty("user.home")).resolve(path)
    } ?: Files.createTempFile("tempPlot", ".html")
    //Files.createDirectories(actualFile.parent)
    val htmlString = createHTML().apply {
        head {
            meta {
                charset = "utf-8"
                headers.forEach {
                    fragment(it)
                }
            }
            title(title)
        }
        body {
            embedVisionFragment(manager, fragment = this@makeFile)
        }
    }.finalize()

    Files.writeString(actualFile, htmlString)
    if (show) {
        Desktop.getDesktop().browse(actualFile.toFile().toURI())
    }
}