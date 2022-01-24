package space.kscience.visionforge.examples

import space.kscience.gdml.GdmlShowCase
import space.kscience.visionforge.gdml.toVision
import space.kscience.visionforge.solid.Solids

fun main() = makeVisionFile {
    vision("canvas") {
        requirePlugin(Solids)
        GdmlShowCase.babyIaxo().toVision()
    }
}