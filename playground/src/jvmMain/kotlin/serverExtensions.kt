package hep.dataforge.vision.examples

import hep.dataforge.context.Context
import hep.dataforge.meta.DFExperimental
import hep.dataforge.vision.html.HtmlVisionFragment
import hep.dataforge.vision.html.ResourceLocation
import hep.dataforge.vision.html.scriptHeader
import hep.dataforge.vision.makeFile
import hep.dataforge.vision.page
import hep.dataforge.vision.three.server.VisionServer
import hep.dataforge.vision.three.server.useScript
import java.awt.Desktop
import java.nio.file.Path


public fun VisionServer.usePlayground(): Unit {
    useScript("js/visionforge-playground.js")
}

@DFExperimental
public fun Context.makeVisionFile(
    content: HtmlVisionFragment,
    path: Path? = null,
    title: String = "VisionForge page",
    resourceLocation: ResourceLocation = ResourceLocation.SYSTEM,
    show: Boolean = true,
): Unit {
    val actualPath = page(title, content).makeFile(path) { actualPath ->
        mapOf("threeJs" to scriptHeader("js/visionforge-playground.js", actualPath, resourceLocation))
    }
    if (show) Desktop.getDesktop().browse(actualPath.toFile().toURI())
}
//    makeVisionFile(fragment, path = path, title = title, show = show) { actualPath ->
//    scriptHeader("js/visionforge-playground.js", actualPath, resourceLocation)
//}
