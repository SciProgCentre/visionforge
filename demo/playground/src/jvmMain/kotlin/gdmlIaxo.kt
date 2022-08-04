package space.kscience.visionforge.examples

import space.kscience.gdml.GdmlShowCase
import space.kscience.visionforge.Colors
import space.kscience.visionforge.gdml.gdml
import space.kscience.visionforge.solid.Solids
import space.kscience.visionforge.solid.ambientLight
import space.kscience.visionforge.solid.set
import space.kscience.visionforge.solid.solid

fun main() = makeVisionFile {
    vision("canvas") {
        requirePlugin(Solids)
        solid {
            ambientLight {
                color.set(Colors.white)
            }
            gdml(GdmlShowCase.babyIaxo(), "D0")
        }
    }
}