package space.kscience.visionforge.examples

import space.kscience.dataforge.context.Context
import space.kscience.dataforge.misc.DFExperimental
import space.kscience.visionforge.html.ResourceLocation
import space.kscience.visionforge.html.VisionTagConsumer
import space.kscience.visionforge.html.page
import space.kscience.visionforge.html.scriptHeader
import space.kscience.visionforge.makeFile
import space.kscience.visionforge.three.server.VisionServer
import space.kscience.visionforge.three.server.useScript
import space.kscience.visionforge.visionManager
import java.awt.Desktop
import java.nio.file.Path


public fun VisionServer.usePlayground(): Unit {
    useScript("js/visionforge-playground.js")
}

@OptIn(DFExperimental::class)
public fun Context.makeVisionFile(
    path: Path? = null,
    title: String = "VisionForge page",
    resourceLocation: ResourceLocation = ResourceLocation.SYSTEM,
    show: Boolean = true,
    content: VisionTagConsumer<*>.() -> Unit,
): Unit {
    val actualPath = visionManager.page(title, content = content).makeFile(path) { actualPath ->
        mapOf(
            "playground" to scriptHeader("js/visionforge-playground.js", resourceLocation, actualPath),
            //"tables" to tabulatorCssHader
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
