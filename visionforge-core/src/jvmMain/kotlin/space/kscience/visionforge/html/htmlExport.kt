package space.kscience.visionforge

import kotlinx.html.body
import kotlinx.html.head
import kotlinx.html.meta
import kotlinx.html.stream.createHTML
import space.kscience.dataforge.context.Global
import space.kscience.dataforge.misc.DFExperimental
import space.kscience.visionforge.html.HtmlFragment
import space.kscience.visionforge.html.VisionPage
import space.kscience.visionforge.html.fragment
import space.kscience.visionforge.html.visionFragment
import java.awt.Desktop
import java.nio.file.Files
import java.nio.file.Path

//
///**
// * Create a full html string (including the head) for a given [HtmlVisionFragment]
// */
//@DFExperimental
//public fun Context.makeVisionString(
//    fragment: HtmlVisionFragment,
//    title: String = "VisionForge page",
//    headerBuilder: () -> HtmlFragment,
//): String = createHTML().apply {
//    head {
//        meta {
//            charset = "utf-8"
//            fragment(headerBuilder())
//        }
//        title(title)
//    }
//    body {
//        embedVisionFragment(visionManager, fragment = fragment)
//    }
//}.finalize()
//
//
///**
// * Make a file with the embedded vision data
// */
//@DFExperimental
//public fun Context.makeVisionFile(
//    fragment: HtmlVisionFragment,
//    path: Path? = null,
//    title: String = "VisionForge page",
//    show: Boolean = true,
//    headerBuilder: (Path) -> HtmlFragment,
//) {
//    val actualFile = path?.let {
//        Path.of(System.getProperty("user.home")).resolve(path)
//    } ?: Files.createTempFile("tempPlot", ".html")
//    //Files.createDirectories(actualFile.parent)
//    val htmlString = makeVisionString(fragment, title) { headerBuilder(actualFile) }
//
//    Files.writeString(actualFile, htmlString)
//    if (show) {
//        Desktop.getDesktop().browse(actualFile.toFile().toURI())
//    }
//}

/**
 * Export a [VisionPage] to a file
 *
 * @param fileHeaders additional file-system specific headers.
 */
@DFExperimental
public fun VisionPage.makeFile(
    path: Path?,
    fileHeaders: ((Path) -> Map<String, HtmlFragment>)? = null,
): Path {
    val actualFile = path?.let {
        Path.of(System.getProperty("user.home")).resolve(path)
    } ?: Files.createTempFile("tempPlot", ".html")

    val actualDefaultHeaders = fileHeaders?.invoke(actualFile)
    val actualHeaders = if (actualDefaultHeaders == null) pageHeaders else actualDefaultHeaders + pageHeaders

    val htmlString = createHTML().apply {
        head {
            meta {
                charset = "utf-8"
            }
            actualHeaders.values.forEach {
                fragment(it)
            }
        }
        body {
            visionFragment(Global, fragment = content)
        }
    }.finalize()

    Files.writeString(actualFile, htmlString)
    return actualFile
}

@DFExperimental
public fun VisionPage.show(path: Path? = null) {
    val actualPath = makeFile(path)
    Desktop.getDesktop().browse(actualPath.toFile().toURI())
}