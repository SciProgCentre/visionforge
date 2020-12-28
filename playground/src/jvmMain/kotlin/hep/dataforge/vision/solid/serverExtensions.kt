package hep.dataforge.vision.solid

import hep.dataforge.context.Context
import hep.dataforge.context.Global
import hep.dataforge.meta.DFExperimental
import hep.dataforge.vision.ResourceLocation
import hep.dataforge.vision.VisionManager
import hep.dataforge.vision.html.HtmlVisionFragment
import hep.dataforge.vision.makeVisionFile
import hep.dataforge.vision.scriptHeader
import hep.dataforge.vision.three.server.VisionServer
import hep.dataforge.vision.three.server.useScript
import java.nio.file.Path


/**
 * A global vision context used to resolve different vision renderers
 */
@DFExperimental
public val visionContext: Context = Global.context("VISION") {
    plugin(VisionManager)
    plugin(SolidManager)
}

public fun VisionServer.usePlayground(): Unit {
    useScript("js/visionforge-playground.js")
}

@DFExperimental
public fun Context.makeVisionFile(
    fragment: HtmlVisionFragment,
    path: Path? = null,
    title: String = "VisionForge page",
    resourceLocation: ResourceLocation = ResourceLocation.SYSTEM,
    show: Boolean = true,
): Unit = makeVisionFile(fragment, path = path, title = title, show = show) { actualPath ->
    scriptHeader("js/visionforge-playground.js", actualPath, resourceLocation)
}
