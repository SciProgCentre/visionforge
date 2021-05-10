package space.kscience.visionforge.gdml.jupyter

import space.kscience.dataforge.misc.DFExperimental
import space.kscience.visionforge.ring.ThreeWithControls
import space.kscience.visionforge.runVisionClient

@DFExperimental
@JsExport
fun main(): Unit = runVisionClient {
    plugin(ThreeWithControls)
}

