package space.kscience.visionforge.examples

import space.kscience.visionforge.solid.ambientLight
import space.kscience.visionforge.solid.extruded
import space.kscience.visionforge.solid.polygon
import space.kscience.visionforge.solid.solid

fun main() = makeVisionFile {
    vision("canvas") {
        solid {
            ambientLight()
            extruded("extruded") {
                shape{
                    polygon(8, 100)
                }
                layer(-30)
                layer(30)
            }
        }
    }
}