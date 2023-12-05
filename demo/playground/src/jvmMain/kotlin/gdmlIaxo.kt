package space.kscience.visionforge.examples

import space.kscience.gdml.GdmlShowCase
import space.kscience.visionforge.gdml.gdml
import space.kscience.visionforge.solid.Solids
import space.kscience.visionforge.solid.solid

fun main() = makeVisionFile {
    vision("canvas") {
        requirePlugin(Solids)
        solid {
            gdml(GdmlShowCase.babyIaxo(), "D0")
        }
    }
}