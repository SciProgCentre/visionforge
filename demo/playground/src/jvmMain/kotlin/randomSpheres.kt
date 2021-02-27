package hep.dataforge.vision.examples

import hep.dataforge.misc.DFExperimental
import hep.dataforge.vision.VisionForge
import hep.dataforge.vision.html.ResourceLocation
import hep.dataforge.vision.html.fragment
import hep.dataforge.vision.invoke
import hep.dataforge.vision.solid.*
import kotlinx.html.h1
import java.nio.file.Paths
import kotlin.random.Random

@OptIn(DFExperimental::class)
fun main() = VisionForge(Solids) {

    val random = Random(112233)
    val fragment = fragment {
        h1 { +"Happy new year!" }
        vision {
            solid {
                repeat(100) {
                    sphere(5, name = "sphere[$it]") {
                        x = random.nextDouble(-300.0, 300.0)
                        y = random.nextDouble(-300.0, 300.0)
                        z = random.nextDouble(-300.0, 300.0)
                        material {
                            color(random.nextInt())
                            specularColor(random.nextInt())
                        }
                        detail = 16
                    }
                }
            }
        }
    }
    makeVisionFile(
        fragment,
        Paths.get("randomSpheres.html"),
        resourceLocation = ResourceLocation.EMBED
    )
}