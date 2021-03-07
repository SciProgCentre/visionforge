package space.kscience.visionforge.examples

import space.kscience.dataforge.misc.DFExperimental
import space.kscience.visionforge.VisionForge
import space.kscience.visionforge.html.ResourceLocation
import space.kscience.visionforge.html.fragment
import space.kscience.visionforge.invoke
import space.kscience.visionforge.solid.Solids
import space.kscience.visionforge.solid.box
import space.kscience.visionforge.solid.solid

@DFExperimental
fun main() = VisionForge(Solids) {
    val content = fragment {
        vision("canvas") {
            solid {
                box(100, 100, 100)
            }
        }
    }
    makeVisionFile(content, resourceLocation = ResourceLocation.SYSTEM)
}