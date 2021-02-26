package hep.dataforge.vision.examples

import hep.dataforge.misc.DFExperimental
import hep.dataforge.vision.VisionForge
import hep.dataforge.vision.html.ResourceLocation
import hep.dataforge.vision.html.fragment
import hep.dataforge.vision.invoke
import hep.dataforge.vision.solid.box
import hep.dataforge.vision.solid.solid
import hep.dataforge.vision.solid.useSolids

@DFExperimental
fun main() = VisionForge.invoke {
    val content = fragment {
        vision("canvas") {
            solid {
                box(100, 100, 100)
            }
        }
    }
    useSolids()
    makeVisionFile(content, resourceLocation = ResourceLocation.SYSTEM)
}