package space.kscience.visionforge.examples

import space.kscience.dataforge.context.Context
import space.kscience.gdml.Gdml
import space.kscience.gdml.LUnit
import space.kscience.gdml.decodeFromStream
import space.kscience.visionforge.gdml.toVision
import space.kscience.visionforge.html.ResourceLocation
import space.kscience.visionforge.solid.Solids

fun main() {
    val context = Context {
        plugin(Solids)
    }
    context.makeVisionFile(resourceLocation = ResourceLocation.EMBED) {
        vision("canvas") {
            Gdml.decodeFromStream(javaClass.getResourceAsStream("/gdml/babyIAXO.gdml")!!, true).toVision {
                lUnit = LUnit.MM
            }
        }
    }
}