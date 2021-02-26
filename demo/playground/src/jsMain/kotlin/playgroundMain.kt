import hep.dataforge.vision.VisionForge
import hep.dataforge.vision.plotly.usePlotly
import hep.dataforge.vision.solid.three.useThreeJs

fun main(): Unit = VisionForge.run{
    usePlotly()
    useThreeJs()
    renderVisionsInWindow()
}