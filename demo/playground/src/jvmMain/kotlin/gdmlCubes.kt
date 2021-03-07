package space.kscience.visionforge.examples

import space.kscience.dataforge.misc.DFExperimental
import space.kscience.visionforge.VisionForge
import space.kscience.visionforge.gdml.GdmlShowcase.cubes
import space.kscience.visionforge.gdml.toVision
import space.kscience.visionforge.html.ResourceLocation
import space.kscience.visionforge.html.fragment
import space.kscience.visionforge.invoke
import space.kscience.visionforge.solid.Solids

@DFExperimental
fun main() = VisionForge(Solids) {
    val content = VisionForge.fragment {
        vision("canvas") {
            cubes.toVision()
        }
    }
    makeVisionFile(content, resourceLocation = ResourceLocation.SYSTEM)
}