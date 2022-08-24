package space.kscience.visionforge.examples

import space.kscience.gdml.GdmlShowCase
import space.kscience.visionforge.gdml.toVision
import space.kscience.visionforge.html.ResourceLocation
import space.kscience.visionforge.solid.Solids

fun main() = makeVisionFile(resourceLocation = ResourceLocation.SYSTEM) {
    vision("canvas") {
        requirePlugin(Solids)
        GdmlShowCase.cubes().toVision()
    }
}