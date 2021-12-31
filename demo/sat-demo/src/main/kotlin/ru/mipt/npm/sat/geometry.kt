package ru.mipt.npm.sat

import space.kscience.dataforge.meta.set
import space.kscience.visionforge.solid.*
import space.kscience.visionforge.style
import space.kscience.visionforge.useStyle
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
    color("darkgreen")
    val transparent by style {
        this[SolidMaterial.MATERIAL_OPACITY_KEY] = 0.3
    }

    val red by style {
        this[SolidMaterial.MATERIAL_COLOR_KEY] = "red"
    }

    val blue by style {
        this[SolidMaterial.MATERIAL_COLOR_KEY] = "blue"
    }

    val totalXSize = xSegments * xSegmentSize.toDouble()
    val totalYSize = ySegments * ySegmentSize.toDouble()
    for (layer in 1..layers) {
        group("layer[$layer]") {
            for (i in 1..xSegments) {
                for (j in 1..ySegments) {
                    box(xSegmentSize, ySegmentSize, layerHeight, name = "segment[$i,$j]") {
                        useStyle(transparent)
                        z = (layer - 0.5) * layerHeight.toDouble()
                        x = (i - 0.5) * xSegmentSize.toDouble()
                        y = (j - 0.5) * ySegmentSize.toDouble()
                    }
                }
            }
            group("fibers") {
                for (i in 1..xSegments) {
                    cylinder(fiberDiameter, totalYSize) {
                        useStyle(red)
                        rotationX = PI / 2
                        z = (layer - 1.0) * layerHeight.toDouble() + fiberDiameter.toDouble()
                        x = (i - 0.5) * xSegmentSize.toDouble()
                        y = totalYSize / 2
                    }
                }

                for (j in 1..ySegments) {
                    cylinder(fiberDiameter, totalXSize) {
                        useStyle(blue)
                        rotationY = PI / 2
                        z = (layer) * layerHeight.toDouble() - fiberDiameter.toDouble()
                        y = (j - 0.5) * xSegmentSize.toDouble()
                        x = totalXSize / 2
                    }
                }
            }
        }
    }
}

