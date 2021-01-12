package hep.dataforge.vision.plotly

import hep.dataforge.meta.DFExperimental
import hep.dataforge.vision.html.HtmlFragment
import hep.dataforge.vision.html.ResourceLocation
import hep.dataforge.vision.html.scriptHeader
import kotlinx.html.script
import kotlinx.html.unsafe
import java.nio.file.Path

internal val plotlyScriptLocation = "js/visionforge-three.js"

/**
 * A header that stores/embeds plotly bundle and registers plotly renderer in the frontend
 */
@OptIn(DFExperimental::class)
public fun plotlyHeader(location: ResourceLocation, filePath: Path? = null): HtmlFragment = {
    scriptHeader(
        plotlyScriptLocation,
        filePath,
        resourceLocation = location
    ).invoke(this)
    script {
        type = "text/javascript"
        unsafe {
            //language=JavaScript
            +"hep.dataforge.vision.plotly.loadPlotly()"
        }
    }
}
