package hep.dataforge.vision.solid

import hep.dataforge.meta.DFExperimental
import hep.dataforge.vision.ResourceLocation
import hep.dataforge.vision.VisionManager
import hep.dataforge.vision.html.fragment
import hep.dataforge.vision.three.server.makeFile
import hep.dataforge.vision.three.server.solid
import kotlinx.html.h1
import java.nio.file.Paths
import kotlin.random.Random

@OptIn(DFExperimental::class)
fun main() {
    val random = Random(112233)
    val fragment = VisionManager.fragment {
        h1 { +"Happy new year!" }
        vision {
            solid {
                repeat(100) {
                    sphere(5) {
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

    fragment.makeFile(Paths.get("stars.html"), resourceLocation = ResourceLocation.EMBED)
}