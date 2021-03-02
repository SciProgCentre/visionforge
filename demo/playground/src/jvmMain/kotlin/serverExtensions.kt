package hep.dataforge.vision.examples

import hep.dataforge.misc.DFExperimental
import hep.dataforge.vision.Vision
import hep.dataforge.vision.VisionForge
import hep.dataforge.vision.html.HtmlVisionFragment
import hep.dataforge.vision.html.ResourceLocation
import hep.dataforge.vision.html.page
import hep.dataforge.vision.html.scriptHeader
import hep.dataforge.vision.makeFile
import hep.dataforge.vision.three.server.VisionServer
import hep.dataforge.vision.three.server.useScript
import java.awt.Desktop
import java.nio.file.Path


public fun VisionServer.usePlayground(): Unit {
    useScript("js/visionforge-playground.js")
}

@DFExperimental
public fun VisionForge.makeVisionFile(
    content: HtmlVisionFragment,
    path: Path? = null,
    title: String = "VisionForge page",
    resourceLocation: ResourceLocation = ResourceLocation.SYSTEM,
    show: Boolean = true,
): Unit {
    val actualPath = page(title, content = content).makeFile(path) { actualPath ->
        mapOf("threeJs" to scriptHeader("js/visionforge-playground.js", actualPath, resourceLocation))
    }
    if (show) Desktop.getDesktop().browse(actualPath.toFile().toURI())
}

@DFExperimental
public fun VisionForge.makeVisionFile(
    vision: Vision,
    path: Path? = null,
    title: String = "VisionForge page",
    resourceLocation: ResourceLocation = ResourceLocation.SYSTEM,
    show: Boolean = true,
): Unit = makeVisionFile({ vision(vision) }, path, title, resourceLocation, show)
