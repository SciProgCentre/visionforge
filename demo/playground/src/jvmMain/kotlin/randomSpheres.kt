package space.kscience.visionforge.examples

import kotlinx.html.div
import kotlinx.html.h1
import space.kscience.dataforge.context.Context
import space.kscience.visionforge.html.ResourceLocation
import space.kscience.visionforge.solid.*
import java.nio.file.Paths
import kotlin.random.Random

fun main() {
    val context = Context {
        plugin(Solids)
    }

    val random = Random(112233)

    context.makeVisionFile(
        Paths.get("randomSpheres.html"),
        resourceLocation = ResourceLocation.EMBED
    ) {
        h1 { +"Happy new year!" }
        div {
            vision {
                solid {
                    repeat(100) {
                        sphere(5, name = "sphere[$it]") {
                            x = random.nextDouble(-300.0, 300.0)
                            y = random.nextDouble(-300.0, 300.0)
                            z = random.nextDouble(-300.0, 300.0)
                            material {
                                color(random.nextInt())
                            }
                            detail = 16
                        }
                    }
                }
            }
        }
    }
}