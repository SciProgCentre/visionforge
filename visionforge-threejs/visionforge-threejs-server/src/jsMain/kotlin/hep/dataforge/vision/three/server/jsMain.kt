package hep.dataforge.vision.three.server

import hep.dataforge.vision.VisionForge
import hep.dataforge.vision.solid.three.useThreeJs

public fun main(): Unit = VisionForge.run {
    useThreeJs()
    renderVisionsInWindow()
}