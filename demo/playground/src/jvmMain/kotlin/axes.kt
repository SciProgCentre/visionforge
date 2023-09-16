package space.kscience.visionforge.examples

import space.kscience.kmath.geometry.Euclidean3DSpace
import space.kscience.kmath.geometry.radians
import space.kscience.visionforge.html.ResourceLocation
import space.kscience.visionforge.solid.*
import kotlin.math.PI

fun main() = makeVisionFile(resourceLocation = ResourceLocation.SYSTEM) {
    vision("canvas") {
        requirePlugin(Solids)
        solid {
            axes(100, "root-axes")
            solidGroup("group") {
                z = 100
                rotate((PI / 4).radians, Euclidean3DSpace.vector(1, 1, 1))
                axes(100, "local-axes")
                box(50, 50, 50, "box")
            }
        }
    }
}