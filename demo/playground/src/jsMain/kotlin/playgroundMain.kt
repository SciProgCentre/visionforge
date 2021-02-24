//import hep.dataforge.vision.plotly.withPlotly
import hep.dataforge.vision.plotly.withPlotly
import hep.dataforge.vision.renderVisionsAt
import hep.dataforge.vision.renderVisionsInWindow
import hep.dataforge.vision.solid.three.withThreeJs
import kotlinx.browser.window

fun main() {
    withPlotly()
    withThreeJs()
    renderVisionsInWindow()
    window.asDynamic()["renderVisionsInWindow"] = ::renderVisionsInWindow
    window.asDynamic()["renderVisionsAt"] = ::renderVisionsAt
}