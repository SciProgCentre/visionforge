package ru.mipt.npm.sat

import hep.dataforge.vision.solid.*
import kotlin.math.PI

internal fun visionOfSatellite(
    layers: Int = 10,
    layerHeight: Number = 10,
    xSegments: Int = 3,
    ySegments: Int = xSegments,
    xSegmentSize: Number = 30,
    ySegmentSize: Number = xSegmentSize,
    fiberDiameter: Number = 1.0,
): SolidGroup = SolidGroup {
    opacity = 0.3
    val totalXSize = xSegments * xSegmentSize.toDouble()
    val totalYSize = ySegments * ySegmentSize.toDouble()
    for (layer in 1..layers) {
        group("layer[$layer]") {
            for (i in 1..xSegments) {
                for (j in 1..ySegments) {
                    box(xSegmentSize, ySegmentSize, layerHeight, name = "segment[$i,$j]") {
                        z = (layer - 0.5) * layerHeight.toDouble()
                        x = (i - 0.5) * xSegmentSize.toDouble()
                        y = (j - 0.5) * ySegmentSize.toDouble()
                    }
                }
            }
            group("fibers") {
                for (i in 1..xSegments) {
                    cylinder(fiberDiameter, totalYSize) {
                        rotationX = PI / 2
                        z = (layer - 1.0) * layerHeight.toDouble() + fiberDiameter.toDouble()
                        x = (i - 0.5) * xSegmentSize.toDouble()
                        y = totalYSize/2

                        color("red")
                    }
                }

                for (j in 1..ySegments) {
                    cylinder(fiberDiameter, totalXSize) {
                        rotationY = PI / 2
                        z = (layer) * layerHeight.toDouble() - fiberDiameter.toDouble()
                        y = (j - 0.5) * xSegmentSize.toDouble()
                        x = totalXSize/2

                        color("blue")
                    }
                }
            }
        }
    }
}

