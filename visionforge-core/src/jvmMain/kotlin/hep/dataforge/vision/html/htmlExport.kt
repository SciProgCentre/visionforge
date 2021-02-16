package hep.dataforge.vision

import hep.dataforge.context.Context
import hep.dataforge.misc.DFExperimental
import hep.dataforge.vision.html.HtmlFragment
import hep.dataforge.vision.html.HtmlVisionFragment
import hep.dataforge.vision.html.Page
import kotlinx.html.stream.createHTML
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

@DFExperimental
public fun Context.page(
    title: String,
    content: HtmlVisionFragment,
    vararg headers: Pair<String, HtmlFragment>,
): Page = Page(this, title, mapOf(*headers), content)


@DFExperimental
public fun Page.makeFile(
    path: Path?,
    defaultHeaders: ((Path) -> Map<String,HtmlFragment>)? = null,
): Path {
    val actualFile = path?.let {
        Path.of(System.getProperty("user.home")).resolve(path)
    } ?: Files.createTempFile("tempPlot", ".html")

    val actualDefaultHeaders = defaultHeaders?.invoke(actualFile)
    val actualPage = if(actualDefaultHeaders == null) this else copy(headers = actualDefaultHeaders + headers)

    val htmlString = actualPage.render(createHTML())

    Files.writeString(actualFile, htmlString)
    return actualFile
}

@DFExperimental
public fun Page.show(path: Path? = null) {
    val actualPath = makeFile(path)
    Desktop.getDesktop().browse(actualPath.toFile().toURI())
}