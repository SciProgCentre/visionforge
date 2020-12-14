package ru.mipt.npm.sat

import hep.dataforge.vision.ResourceLocation
import hep.dataforge.vision.VisionManager
import hep.dataforge.vision.html.fragment
import hep.dataforge.vision.solid.box
import hep.dataforge.vision.three.server.makeFile
import hep.dataforge.vision.three.server.solid

fun main() {
    val fragment = VisionManager.fragment {
        vision("canvas") {
            solid {
                box(100, 100, 100)
            }
        }
    }

    fragment.makeFile(resourceLocation = ResourceLocation.LOCAL)
}