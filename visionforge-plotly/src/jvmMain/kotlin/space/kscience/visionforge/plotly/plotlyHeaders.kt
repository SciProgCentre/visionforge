package space.kscience.visionforge.plotly

import kotlinx.html.script
import kotlinx.html.unsafe
import space.kscience.dataforge.misc.DFExperimental
import space.kscience.visionforge.html.HtmlFragment
import space.kscience.visionforge.html.ResourceLocation
import space.kscience.visionforge.html.scriptHeader
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
            +"space.kscience.visionforge.plotly.loadPlotly()"
        }
    }
}
