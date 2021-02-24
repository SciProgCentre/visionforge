package hep.dataforge.vision.gdml

import hep.dataforge.names.toName
import hep.dataforge.vision.solid.SolidGroup
import hep.dataforge.vision.solid.SolidManager
import org.junit.jupiter.api.Test
import space.kscience.gdml.*
import kotlin.test.assertNotNull

class TestCubes {
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

    @Test
    fun testCubesDirect(){
        val vision = cubes.toVision()
        assertNotNull(vision.getPrototype("solids.smallBox".toName()))
    }

    @Test
    fun testCubesReSerialize(){
        val vision = cubes.toVision()
        val serialized = SolidManager.encodeToString(vision)
        val deserialized = SolidManager.decodeFromString(serialized) as SolidGroup
        assertNotNull(deserialized.getPrototype("solids.smallBox".toName()))
    }
}