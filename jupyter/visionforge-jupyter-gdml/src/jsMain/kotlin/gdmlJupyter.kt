package space.kscience.visionforge.gdml.jupyter

import space.kscience.dataforge.misc.DFExperimental
import space.kscience.visionforge.ring.ThreeWithControlsPlugin
import space.kscience.visionforge.runVisionClient

@DFExperimental
@JsExport
public fun main(): Unit = runVisionClient {
    plugin(ThreeWithControlsPlugin)
}

