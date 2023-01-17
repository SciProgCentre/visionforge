package ru.mipt.npm.sat

import space.kscience.dataforge.misc.DFExperimental
import space.kscience.visionforge.html.ResourceLocation
import space.kscience.visionforge.solid.box
import space.kscience.visionforge.solid.material
import space.kscience.visionforge.solid.set
import space.kscience.visionforge.solid.solid
import space.kscience.visionforge.three.makeThreeJsFile

@OptIn(DFExperimental::class)
fun main() = makeThreeJsFile(resourceLocation = ResourceLocation.SYSTEM) {
    vision ("canvas") {
        solid {
            box(100, 100, 100)
            material {
                emissiveColor.set("red")
            }
        }
    }
}