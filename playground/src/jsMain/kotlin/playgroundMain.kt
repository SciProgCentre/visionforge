import hep.dataforge.vision.plotly.withPlotly
import hep.dataforge.vision.renderVisionsInWindow
import hep.dataforge.vision.solid.three.loadThreeJs

fun main() {
    withPlotly()
    loadThreeJs()
    renderVisionsInWindow()
}