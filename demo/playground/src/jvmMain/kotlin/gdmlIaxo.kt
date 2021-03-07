package space.kscience.visionforge.examples

import space.kscience.dataforge.misc.DFExperimental
import space.kscience.gdml.Gdml
import space.kscience.gdml.LUnit
import space.kscience.gdml.decodeFromStream
import space.kscience.visionforge.VisionForge
import space.kscience.visionforge.gdml.toVision
import space.kscience.visionforge.html.ResourceLocation
import space.kscience.visionforge.html.fragment
import space.kscience.visionforge.invoke
import space.kscience.visionforge.solid.Solids

@DFExperimental
fun main() = VisionForge(Solids) {
    val content = VisionForge.fragment {
        vision("canvas") {
            Gdml.decodeFromStream(Gdml.javaClass.getResourceAsStream("/gdml/babyIAXO.gdml")!!, true).toVision {
                lUnit = LUnit.MM
            }
        }
    }
    makeVisionFile(content, resourceLocation = ResourceLocation.EMBED)
}