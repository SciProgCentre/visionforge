package hep.dataforge.vision.examples

import hep.dataforge.misc.DFExperimental
import hep.dataforge.vision.VisionForge
import hep.dataforge.vision.gdml.GdmlShowcase.cubes
import hep.dataforge.vision.gdml.toVision
import hep.dataforge.vision.html.ResourceLocation
import hep.dataforge.vision.html.fragment
import hep.dataforge.vision.invoke
import hep.dataforge.vision.solid.Solids

@DFExperimental
fun main() = VisionForge(Solids) {
    val content = VisionForge.fragment {
        vision("canvas") {
            cubes.toVision()
        }
    }
    makeVisionFile(content, resourceLocation = ResourceLocation.SYSTEM)
}