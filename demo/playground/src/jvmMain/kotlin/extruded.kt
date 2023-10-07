package space.kscience.visionforge.examples

import space.kscience.visionforge.solid.*

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
            }.apply {
                edges(false)
            }
        }
    }
}