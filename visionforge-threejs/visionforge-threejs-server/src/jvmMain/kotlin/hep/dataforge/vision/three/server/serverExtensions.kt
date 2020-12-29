package hep.dataforge.vision.three.server

import hep.dataforge.context.Context
import hep.dataforge.meta.DFExperimental
import hep.dataforge.vision.ResourceLocation
import hep.dataforge.vision.html.HtmlVisionFragment
import hep.dataforge.vision.makeVisionFile
import hep.dataforge.vision.scriptHeader
import java.nio.file.Path


public fun VisionServer.useThreeJs(): Unit {
    useScript("js/visionforge-three.js")
}

@DFExperimental
public fun Context.makeVisionFile(
    fragment: HtmlVisionFragment,
    path: Path? = null,
    title: String = "VisionForge page",
    resourceLocation: ResourceLocation = ResourceLocation.SYSTEM,
    show: Boolean = true,
): Unit = makeVisionFile(fragment, path = path, title = title, show = show) { actualPath ->
    scriptHeader("js/visionforge-three.js", actualPath, resourceLocation)
}