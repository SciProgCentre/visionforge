package space.kscience.visionforge.three

import space.kscience.dataforge.misc.DFExperimental
import space.kscience.visionforge.html.runVisionClient
import space.kscience.visionforge.solid.three.ThreePlugin


@DFExperimental
public fun main(): Unit = runVisionClient {
    plugin(ThreePlugin)
}