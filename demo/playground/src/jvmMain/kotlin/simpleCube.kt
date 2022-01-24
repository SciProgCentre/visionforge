package space.kscience.visionforge.examples

import space.kscience.visionforge.html.ResourceLocation
import space.kscience.visionforge.solid.box
import space.kscience.visionforge.solid.invoke
import space.kscience.visionforge.solid.material
import space.kscience.visionforge.solid.solid

fun main() = makeVisionFile(resourceLocation = ResourceLocation.SYSTEM) {
    vision("canvas") {
        solid {
            box(100, 100, 100)
            material {
                emissiveColor("red")
            }
        }
    }
}