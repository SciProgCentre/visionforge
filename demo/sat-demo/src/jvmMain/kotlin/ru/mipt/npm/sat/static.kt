package ru.mipt.npm.sat

import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.request
import space.kscience.dataforge.misc.DFExperimental
import space.kscience.visionforge.html.ResourceLocation
import space.kscience.visionforge.html.vision
import space.kscience.visionforge.solid.*
import space.kscience.visionforge.three.makeThreeJsFile

val context = Context("Sat-server") {
    plugin(Solids)
}


@OptIn(DFExperimental::class)
fun main() = context.request(Solids).makeThreeJsFile(resourceLocation = ResourceLocation.SYSTEM) {
    vision("canvas") {
        solid {
            box(100, 100, 100)
            material {
                emissiveColor("red")
            }
        }
    }
}