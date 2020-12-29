package hep.dataforge.vision.solid

import hep.dataforge.meta.DFExperimental
import hep.dataforge.vision.ResourceLocation
import hep.dataforge.vision.VisionManager
import hep.dataforge.vision.html.fragment

@OptIn(DFExperimental::class)
fun main() {
    val fragment = VisionManager.fragment {
        vision("canvas") {
            solid {
                box(100, 100, 100)
            }
        }
    }

    VisionForge.makeVisionFile(fragment = fragment, resourceLocation = ResourceLocation.SYSTEM)
}