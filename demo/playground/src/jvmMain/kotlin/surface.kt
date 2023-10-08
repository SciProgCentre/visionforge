package space.kscience.visionforge.examples

import space.kscience.visionforge.solid.ambientLight
import space.kscience.visionforge.solid.polygon
import space.kscience.visionforge.solid.solid
import space.kscience.visionforge.solid.surface

fun main() = makeVisionFile {
    vision("canvas") {
        solid {
            ambientLight()
            surface("surface") {
                layer(0, { polygon(8, 10) }, { polygon(8, 20) })
                layer(10, { polygon(8, 20) }, { polygon(8, 30) })
                layer(20, { polygon(8, 10) }, { polygon(8, 20) })
            }
        }
    }
}