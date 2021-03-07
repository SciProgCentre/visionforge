package space.kscience.visionforge.three.server

import kotlinx.browser.window
import space.kscience.dataforge.misc.DFExperimental
import space.kscience.visionforge.VisionForge
import space.kscience.visionforge.solid.three.useThreeJs

@DFExperimental
public fun main(): Unit = VisionForge.run {
    useThreeJs()
    renderVisionsInWindow()
    window.asDynamic()["renderVisionsAt"] = ::renderVisionsAt
    window.asDynamic()["renderVisionsInWindow"] = ::renderVisionsInWindow
}