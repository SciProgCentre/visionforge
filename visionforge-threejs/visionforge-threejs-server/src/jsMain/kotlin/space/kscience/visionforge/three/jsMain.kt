package space.kscience.visionforge.three

import space.kscience.dataforge.misc.DFExperimental
import space.kscience.visionforge.ring.ThreeWithControlsPlugin
import space.kscience.visionforge.runVisionClient


@DFExperimental
public fun main(): Unit = runVisionClient {
    plugin(ThreeWithControlsPlugin)
}