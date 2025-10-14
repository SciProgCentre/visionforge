package space.kscience.plotly

import kotlinx.html.script
import kotlinx.html.unsafe
import space.kscience.visionforge.html.*
import space.kscience.visionforge.visionManager
import java.awt.Desktop
import java.nio.file.Files
import java.nio.file.Path
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter


internal const val assetsDirectory = "assets"

/**
 * Create a standalone html with the plot
 * @param path the reference to html file. If null, create a temporary file
 * @param resourceLocation specifies where to store resources for page display
 * @param config represents plotly frame configuration
 */
public fun Plot.makeFile(
    path: Path,
    vararg headers: HtmlFragment = emptyArray(),
    resourceLocation: ResourceLocation = ResourceLocation.LOCAL,
    config: PlotlyConfig = PlotlyConfig(),
) {
    Files.createDirectories(path.parent)
    Files.writeString(path, toHTMLPage(inferPlotlyHeader(path, resourceLocation), *headers, config = config))
}

public fun Plot.openInBrowser(
    vararg headers: HtmlFragment = emptyArray(),
    path: Path = Files.createTempFile("vfPlot", ".html"),
    resourceLocation: ResourceLocation = ResourceLocation.LOCAL,
    config: PlotlyConfig = PlotlyConfig(),
) {
    makeFile(path, *headers, resourceLocation = resourceLocation, config = config)
    Desktop.getDesktop().browse(path.toFile().toURI())
}

public fun Plotly.makePageFile(
    path: Path? = null,
    title: String = "VisionForge Plotly page",
    additionalHeaders: Map<String, HtmlFragment> = emptyMap(),
    resourceLocation: ResourceLocation = ResourceLocation.SYSTEM,
    show: Boolean = true,
    content: HtmlVisionFragment,
): Path {
    val actualPath = VisionPage(context.visionManager, content = content).makeFile(path) { actualPath ->
        mapOf(
            "title" to VisionPage.title(title),
            "plotly" to VisionPage.importScriptHeader(
                "js/plotly-kt.js",
                resourceLocation,
                actualPath
            ),
            "plotly-render" to HtmlFragment {
                script {
                    defer = true

                    unsafe {
                        +"""
                            window.renderAllPlots()
                        """.trimIndent()
                    }
                }
            }
        ) + additionalHeaders
    }
    if (show) Desktop.getDesktop().browse(actualPath.toFile().toURI())
    return actualPath
}

///**
// * The same as [Plot.makeFile].
// */
//public fun PlotlyFragment.makeFile(
//    path: Path? = null,
//    show: Boolean = true,
//    title: String = "Plotly.kt",
//    resourceLocation: ResourceLocation = ResourceLocation.LOCAL,
//    additionalHeaders: List<HtmlFragment> = emptyList(),
//) {
//    toPage(
//        title = title,
//        headers = (additionalHeaders + inferPlotlyHeader(path, resourceLocation)).toTypedArray()
//    ).makeFile(path, show)
//}
//
//
///**
// * Export a page html to a file.
// */
//public fun PlotlyPage.makeFile(path: Path? = null, show: Boolean = true) {
//    val actualFile = path ?: Files.createTempFile("tempPlot", ".html")
//    Files.createDirectories(actualFile.parent)
//    Files.writeString(actualFile, render())
//    if (show) {
//        Desktop.getDesktop().browse(actualFile.toFile().toURI())
//    }
//}
//
//public fun Plotly.display(pageBuilder: FlowContent.(renderer: PlotlyRenderer) -> Unit): Unit =
//    fragment(pageBuilder).makeFile(null, true)

/**
 * Select a file to save plot to using a Swing form.
 */
@UnstablePlotlyAPI
public fun Plotly.selectFile(filter: FileNameExtensionFilter? = null): Path? {
    val fileChooser = JFileChooser()
    fileChooser.dialogTitle = "Specify a file to save"
    if (filter != null) {
        fileChooser.fileFilter = filter
    }

    val userSelection = fileChooser.showSaveDialog(null)

    return if (userSelection == JFileChooser.APPROVE_OPTION) {
        val fileToSave = fileChooser.selectedFile
        fileToSave.toPath()
    } else {
        null
    }
}

/**
 * Output format for Orca export
 */
public enum class OrcaFormat {
    png, jpeg, webp, svg, pdf
}

/**
 * Use external [plotly-orca] (https://github.com/plotly/orca) tool to export static image.
 * The tool must be installed externally (for example via conda) and usage patterns could differ for different systems.
 */
@UnstablePlotlyAPI
public fun Plot.export(path: Path, format: OrcaFormat = OrcaFormat.svg) {
    val tempFile = Files.createTempFile("plotly-orca-export", ".json")
    Files.writeString(tempFile, toJsonString())

    val command = if (System.getProperty("os.name").contains("Windows")) {
        "powershell"
    } else {
        "bash"
    }

    val process = ProcessBuilder(
        command, "orca", "graph", tempFile.toAbsolutePath().toString(),
        "-f", format.toString(),
        "-d", path.parent.toString(),
        "-o", path.fileName.toString(),
        "--verbose"
    ).redirectErrorStream(true).start()
    process.outputStream.close()
    val output = process.inputStream.bufferedReader().use { it.readText() }
    println(output)
    println("Orca finished with code: ${process.waitFor()}")
}