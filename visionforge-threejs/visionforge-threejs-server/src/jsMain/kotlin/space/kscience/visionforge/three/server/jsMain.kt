package space.kscience.visionforge.three.server

import kotlinx.browser.window
import space.kscience.dataforge.misc.DFExperimental
import space.kscience.visionforge.VisionForge
import space.kscience.visionforge.solid.three.useThreeJs

@DFExperimental
public fun main(): Unit = VisionForge.run {
    console.info("Starting VisionForge context")
    useThreeJs()
    window.asDynamic()["VisionForge"] = VisionForge
    renderVisionsInWindow()
}