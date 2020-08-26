package hep.dataforge.vision.gdml.demo

import scientifik.gdml.*


fun cubes(): GDML = GDML {
    val center = define.position("center")
    structure {
        val air = ref<GDMLMaterial>("G4_AIR")
        val tubeMaterial = ref<GDMLMaterial>("tube")
        val boxMaterial = ref<GDMLMaterial>("box")
        val segment = solids.tube("segment", 20, 5.0) {
            rmin = 17
            deltaphi = 60
            aunit = AUnit.DEG.title
        }
        val worldBox = solids.box("LargeBox", 200, 200, 200)
        val smallBox = solids.box("smallBox", 30, 30, 30)
        val segmentVolume = volume("segment", tubeMaterial, segment.ref()) {}
        val circle = volume("composite", boxMaterial, smallBox.ref()) {
            for (i in 0 until 6) {
                physVolume(segmentVolume) {
                    name = "segment_$i"
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
                            name = "composite$i$j$k"
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