package space.kscience.visionforge.three.server

import space.kscience.dataforge.misc.DFExperimental
import space.kscience.visionforge.VisionManager
import space.kscience.visionforge.html.HtmlVisionFragment
import space.kscience.visionforge.html.ResourceLocation
import space.kscience.visionforge.html.page
import space.kscience.visionforge.html.scriptHeader
import space.kscience.visionforge.makeFile
import java.awt.Desktop
import java.nio.file.Path


public fun VisionServer.useThreeJs(): Unit {
    useScript("js/visionforge-three.js")
}

@DFExperimental
public fun VisionManager.makeThreeJsFile(
    content: HtmlVisionFragment,
    path: Path? = null,
    title: String = "VisionForge page",
    resourceLocation: ResourceLocation = ResourceLocation.SYSTEM,
    show: Boolean = true,
): Unit {
    val actualPath = page(title, content = content).makeFile(path) { actualPath ->
        mapOf("threeJs" to scriptHeader("js/visionforge-three.js", resourceLocation, actualPath))
    }
    if (show) Desktop.getDesktop().browse(actualPath.toFile().toURI())
}
