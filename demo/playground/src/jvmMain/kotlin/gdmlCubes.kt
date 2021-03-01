package hep.dataforge.vision.examples

import hep.dataforge.misc.DFExperimental
import hep.dataforge.vision.VisionForge
import hep.dataforge.vision.gdml.toVision
import hep.dataforge.vision.html.ResourceLocation
import hep.dataforge.vision.html.fragment
import hep.dataforge.vision.invoke
import hep.dataforge.vision.solid.Solids
import space.kscience.gdml.*

internal val cubes = Gdml {
    val center = define.position("center")
    structure {
        val air = materials.composite("G4_AIR") {}
        val tubeMaterial = materials.composite("tube") {}
        val boxMaterial = materials.composite("box") {}

        val segment = solids.tube("segment", 20, 5.0) {
            rmin = 17
            deltaphi = 60
            aunit = AUnit.DEG.title
        }
        val worldBox = solids.box("largeBox", 200, 200, 200)
        val smallBox = solids.box("smallBox", 30, 30, 30)
        val segmentVolume = volume("segment", tubeMaterial, segment) {}
        val circle = volume("composite", boxMaterial, smallBox) {
            for (i in 0 until 6) {
                physVolume(segmentVolume) {
                    positionref = center
                    rotation {
                        z = 60 * i
                        unit = AUnit.DEG.title
                    }
                }
            }
        }

        world = volume("world", air, worldBox) {
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
fun main() = VisionForge(Solids) {
    val content = VisionForge.fragment {
        vision("canvas") {
            cubes.toVision()
        }
    }
    makeVisionFile(content, resourceLocation = ResourceLocation.SYSTEM)
}