import kotlinx.browser.window
import space.kscience.dataforge.misc.DFExperimental
import space.kscience.visionforge.VisionForge
import space.kscience.visionforge.plotly.usePlotly
import space.kscience.visionforge.solid.three.useThreeJs

@DFExperimental
fun main(): Unit = VisionForge.run{
    usePlotly()
    useThreeJs()
    renderVisionsInWindow()
    window.asDynamic()["renderVisionsAt"] = ::renderVisionsAt
    window.asDynamic()["renderVisionsInWindow"] = ::renderVisionsInWindow
}