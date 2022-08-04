package space.kscience.visionforge.examples

import kotlinx.html.div
import kotlinx.html.h1
import space.kscience.visionforge.Colors
import space.kscience.visionforge.html.ResourceLocation
import space.kscience.visionforge.solid.*
import java.nio.file.Paths
import kotlin.random.Random

private val random = Random(112233)

fun main() = makeVisionFile(
    Paths.get("randomSpheres.html"),
    resourceLocation = ResourceLocation.SYSTEM
) {
    h1 { +"Happy new year!" }
    div {
        vision {
            solid {
                ambientLight {
                    color.set(Colors.white)
                }
                repeat(100) {
                    sphere(5, name = "sphere[$it]") {
                        x = random.nextDouble(-300.0, 300.0)
                        y = random.nextDouble(-300.0, 300.0)
                        z = random.nextDouble(-300.0, 300.0)
                        material {
                            color.set(random.nextInt())
                        }
                        detail = 16
                    }
                }
            }
        }
    }
}