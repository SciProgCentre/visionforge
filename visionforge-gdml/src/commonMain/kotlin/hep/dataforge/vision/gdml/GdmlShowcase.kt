package hep.dataforge.vision.gdml

import space.kscience.gdml.*

public object GdmlShowcase {
    public val cubes: Gdml = Gdml {
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
                        name = "segment[$i]"
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
                                name = "composite[$i,$j,$k]"
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
}