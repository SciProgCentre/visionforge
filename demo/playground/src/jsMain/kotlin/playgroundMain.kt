import hep.dataforge.misc.DFExperimental
import hep.dataforge.vision.VisionForge
import hep.dataforge.vision.plotly.usePlotly
import hep.dataforge.vision.solid.three.useThreeJs
import kotlinx.browser.window

@DFExperimental
fun main(): Unit = VisionForge.run{
    usePlotly()
    useThreeJs()
    renderVisionsInWindow()
    window.asDynamic()["renderVisionsAt"] = ::renderVisionsAt
    window.asDynamic()["renderVisionsInWindow"] = ::renderVisionsInWindow
}