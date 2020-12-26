package hep.dataforge.vision.three.server

import hep.dataforge.context.Context
import hep.dataforge.context.Global
import hep.dataforge.meta.DFExperimental
import hep.dataforge.vision.ResourceLocation
import hep.dataforge.vision.html.HtmlVisionFragment
import hep.dataforge.vision.html.VisionOutput
import hep.dataforge.vision.makeFile
import hep.dataforge.vision.scriptHeader
import hep.dataforge.vision.solid.SolidGroup
import hep.dataforge.vision.solid.SolidManager
import java.nio.file.Files
import java.nio.file.Path

public actual val visionContext: Context = Global.context("vision-server") {
    //Loading solid manager for the backend (it does not know about three
    plugin(SolidManager)
}

public fun VisionServer.useThreeJs(): Unit {
    useScript("js/visionforge-three.js")
//    header {
//        script {
//            unsafe {
//                +"renderThreeVisions()"
//            }
//        }
//    }
}

@DFExperimental
public inline fun VisionOutput.solid(block: SolidGroup.() -> Unit): SolidGroup = SolidGroup().apply(block)

@OptIn(DFExperimental::class)
public fun HtmlVisionFragment.makeFile(
    path: Path? = null,
    title: String = "VisionForge page",
    resourceLocation: ResourceLocation = ResourceLocation.SYSTEM,
    show: Boolean = true,
) {
    val actualPath = path?.let {
        Path.of(System.getProperty("user.home")).resolve(path)
    } ?: Files.createTempFile("tempPlot", ".html")
    val scriptHeader = Context.scriptHeader("js/visionforge-three.js", actualPath, resourceLocation)
    makeFile(visionManager, path = path, show = show, title = title, headers = arrayOf(scriptHeader))
}