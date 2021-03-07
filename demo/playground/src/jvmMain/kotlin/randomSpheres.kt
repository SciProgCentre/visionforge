package space.kscience.visionforge.examples

import kotlinx.html.h1
import space.kscience.dataforge.misc.DFExperimental
import space.kscience.visionforge.VisionForge
import space.kscience.visionforge.html.ResourceLocation
import space.kscience.visionforge.html.fragment
import space.kscience.visionforge.invoke
import space.kscience.visionforge.solid.*
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