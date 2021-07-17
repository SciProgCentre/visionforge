package space.kscience.visionforge.examples

import space.kscience.dataforge.context.Context
import space.kscience.visionforge.html.ResourceLocation
import space.kscience.visionforge.solid.Solids
import space.kscience.visionforge.solid.box
import space.kscience.visionforge.solid.solid

fun main() {
    val context = Context {
        plugin(Solids)
    }

    context.makeVisionFile(resourceLocation = ResourceLocation.SYSTEM){
        vision("canvas") {
            solid {
                box(100, 100, 100)
            }
        }
    }
}