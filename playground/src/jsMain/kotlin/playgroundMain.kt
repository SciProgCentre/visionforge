import hep.dataforge.context.Context
import hep.dataforge.context.Global
import hep.dataforge.meta.DFExperimental
import hep.dataforge.vision.client.VisionClient
import hep.dataforge.vision.client.renderAllVisions
import hep.dataforge.vision.plotly.PlotlyPlugin
import hep.dataforge.vision.solid.three.ThreePlugin
import kotlinx.browser.window

@DFExperimental
fun main() {

    val visionContext: Context = Global.context("VISION") {
        plugin(ThreePlugin)
        plugin(PlotlyPlugin)
        plugin(VisionClient)
    }

    //Loading three-js renderer
    val clientManager = visionContext.plugins.fetch(VisionClient)

    //Fetch from server and render visions for all outputs
    window.onload = {
        clientManager.renderAllVisions()
    }
    //startApplication(::PlayGroundApp)
}