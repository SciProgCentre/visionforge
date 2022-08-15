package space.kscience.visionforge.examples

import space.kscience.gdml.GdmlShowCase
import space.kscience.visionforge.Colors
import space.kscience.visionforge.gdml.toVision
import space.kscience.visionforge.html.ResourceLocation
import space.kscience.visionforge.solid.Solids
import space.kscience.visionforge.solid.ambientLight
import space.kscience.visionforge.solid.set

fun main() = makeVisionFile(resourceLocation = ResourceLocation.SYSTEM) {
    vision("canvas") {
        requirePlugin(Solids)
        GdmlShowCase.cubes().toVision().also {
            it.ambientLight {
                color.set(Colors.white)
            }
        }
    }
}