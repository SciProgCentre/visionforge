package hep.dataforge.vision.examples

import hep.dataforge.misc.DFExperimental
import hep.dataforge.vision.VisionForge
import hep.dataforge.vision.VisionManager
import hep.dataforge.vision.gdml.gdml
import hep.dataforge.vision.html.ResourceLocation
import hep.dataforge.vision.html.fragment
import hep.dataforge.vision.solid.solid
import hep.dataforge.vision.solid.withSolids
import space.kscience.gdml.*

internal val cubes = Gdml {
    val center = define.position("center")
    structure {
        val air = ref<GdmlMaterial>("G4_AIR")
        val tubeMaterial = ref<GdmlMaterial>("tube")
        val boxMaterial = ref<GdmlMaterial>("box")

        val segment = solids.tube("segment", 20, 5.0) {
            rmin = 17
            deltaphi = 60
            aunit = AUnit.DEG.title
        }
        val worldBox = solids.box("largeBox", 200, 200, 200)
        val smallBox = solids.box("smallBox", 30, 30, 30)
        val segmentVolume = volume("segment", tubeMaterial, segment.ref()) {}
        val circle = volume("composite", boxMaterial, smallBox.ref()) {
            for (i in 0 until 6) {
                physVolume(segmentVolume) {
                    positionref = center.ref()
                    rotation {
                        z = 60 * i
                        unit = AUnit.DEG.title
                    }
                }
            }
        }

        world = volume("world", air, worldBox.ref()) {
            for (i in 0 until 3) {
                for (j in 0 until 3) {
                    for (k in 0 until 3) {
                        physVolume(circle) {
                            position {
                                x = (-50 + i * 50)
                                y = (-50 + j * 50)
                                z = (-50 + k * 50)
                            }
                            rotation {
                                x = i * 120
                                y = j * 120
                                z = 120 * k
                            }
                        }
                    }
                }
            }
        }
    }
}

@DFExperimental
fun main() {
    val content = VisionManager.fragment {
        vision("canvas") {
            solid {
                gdml(cubes)
            }
        }
    }

    VisionForge.withSolids().makeVisionFile(content, resourceLocation = ResourceLocation.SYSTEM)
}