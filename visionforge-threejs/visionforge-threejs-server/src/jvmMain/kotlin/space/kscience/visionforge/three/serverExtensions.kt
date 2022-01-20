package space.kscience.visionforge.three

import space.kscience.dataforge.context.Global
import space.kscience.dataforge.misc.DFExperimental
import space.kscience.visionforge.html.*
import space.kscience.visionforge.makeFile
import java.awt.Desktop
import java.nio.file.Path


public val Page.Companion.threeJsHeader: HtmlFragment get() = scriptHeader("js/visionforge-three.js")


@DFExperimental
public fun makeThreeJsFile(
    path: Path? = null,
    title: String = "VisionForge page",
    resourceLocation: ResourceLocation = ResourceLocation.SYSTEM,
    show: Boolean = true,
    content: HtmlVisionFragment,
): Unit {
    val actualPath = Page(Global, content = content).makeFile(path) { actualPath ->
        mapOf(
            "title" to Page.title(title),
            "threeJs" to Page.importScriptHeader("js/visionforge-three.js", resourceLocation, actualPath)
        )
    }
    if (show) Desktop.getDesktop().browse(actualPath.toFile().toURI())
}
