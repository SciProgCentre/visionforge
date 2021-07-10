package space.kscience.visionforge.examples

import space.kscience.dataforge.context.Context
import space.kscience.gdml.GdmlShowCase
import space.kscience.visionforge.gdml.toVision
import space.kscience.visionforge.html.ResourceLocation
import space.kscience.visionforge.solid.Solids

fun main() {
    val context = Context {
        plugin(Solids)
    }

    context.makeVisionFile(resourceLocation = ResourceLocation.EMBED) {
        vision("canvas") { GdmlShowCase.babyIaxo().toVision() }
    }
}