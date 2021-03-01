package hep.dataforge.vision.three.server

import hep.dataforge.misc.DFExperimental
import hep.dataforge.vision.VisionForge
import hep.dataforge.vision.solid.three.useThreeJs

@DFExperimental
public fun main(): Unit = VisionForge.run {
    useThreeJs()
    renderVisionsInWindow()
}