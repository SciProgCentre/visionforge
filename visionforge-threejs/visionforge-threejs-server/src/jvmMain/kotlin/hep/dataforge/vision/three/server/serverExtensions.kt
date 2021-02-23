package hep.dataforge.vision.three.server

import hep.dataforge.context.Context
import hep.dataforge.misc.DFExperimental
import hep.dataforge.vision.html.HtmlVisionFragment
import hep.dataforge.vision.html.ResourceLocation
import hep.dataforge.vision.html.page
import hep.dataforge.vision.html.scriptHeader
import hep.dataforge.vision.makeFile
import java.awt.Desktop
import java.nio.file.Path


public fun VisionServer.useThreeJs(): Unit {
    useScript("js/visionforge-three.js")
}

@DFExperimental
public fun Context.makeThreeJsFile(
    content: HtmlVisionFragment,
    path: Path? = null,
    title: String = "VisionForge page",
    resourceLocation: ResourceLocation = ResourceLocation.SYSTEM,
    show: Boolean = true,
): Unit {
    val actualPath = page(title, content).makeFile(path) { actualPath ->
        mapOf("threeJs" to scriptHeader("js/visionforge-three.js", actualPath, resourceLocation))
    }
    if (show) Desktop.getDesktop().browse(actualPath.toFile().toURI())
}
