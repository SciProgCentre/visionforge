package space.kscience.visionforge.examples

import space.kscience.dataforge.context.Global
import space.kscience.visionforge.html.HtmlVisionFragment
import space.kscience.visionforge.html.Page
import space.kscience.visionforge.html.ResourceLocation
import space.kscience.visionforge.html.importScriptHeader
import space.kscience.visionforge.makeFile
import java.awt.Desktop
import java.nio.file.Path

public fun makeVisionFile(
    path: Path? = null,
    title: String = "VisionForge page",
    resourceLocation: ResourceLocation = ResourceLocation.SYSTEM,
    show: Boolean = true,
    content: HtmlVisionFragment,
): Unit {
    val actualPath = Page(Global, content = content).makeFile(path) { actualPath ->
        mapOf(
            "title" to Page.title(title),
            "playground" to Page.importScriptHeader("js/visionforge-playground.js", resourceLocation, actualPath),
        )
    }
    if (show) Desktop.getDesktop().browse(actualPath.toFile().toURI())
}

//@DFExperimental
//public fun Context.makeVisionFile(
//    vision: Vision,
//    path: Path? = null,
//    title: String = "VisionForge page",
//    resourceLocation: ResourceLocation = ResourceLocation.SYSTEM,
//    show: Boolean = true,
//): Unit = makeVisionFile({ vision(vision) }, path, title, resourceLocation, show)
