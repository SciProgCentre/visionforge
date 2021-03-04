package hep.dataforge.vision.examples

import hep.dataforge.misc.DFExperimental
import hep.dataforge.vision.VisionForge
import hep.dataforge.vision.gdml.toVision
import hep.dataforge.vision.html.ResourceLocation
import hep.dataforge.vision.html.fragment
import hep.dataforge.vision.invoke
import hep.dataforge.vision.solid.Solids
import space.kscience.gdml.Gdml
import space.kscience.gdml.LUnit
import space.kscience.gdml.decodeFromStream

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