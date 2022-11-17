package space.kscience.visionforge.examples

import space.kscience.dataforge.context.Global
import space.kscience.visionforge.html.HtmlVisionFragment
import space.kscience.visionforge.html.ResourceLocation
import space.kscience.visionforge.html.VisionPage
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
    val actualPath = VisionPage(Global, content = content).makeFile(path) { actualPath ->
        mapOf(
            "title" to VisionPage.title(title),
            "playground" to VisionPage.importScriptHeader("js/visionforge-playground.js", resourceLocation, actualPath),
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
