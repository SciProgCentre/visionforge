package ru.mipt.npm.muon.monitor

import hep.dataforge.vis.spatial.Point3D
import hep.dataforge.vis.spatial.plus
import ru.mipt.npm.muon.monitor.Monitor.PIXEL_XY_SIZE
import ru.mipt.npm.muon.monitor.Monitor.PIXEL_Z_SIZE

/**
 * A single pixel
 */
open class SC1(
    val name: String,
    val center: Point3D,
    val xSize: Double = PIXEL_XY_SIZE, val ySize: Double = PIXEL_XY_SIZE, val zSize: Double = PIXEL_Z_SIZE
)

class SC16(
    val name: String,
    val center: Point3D
) {

    /**
     * Build map for single SC16 detector
     */
    val pixels: Collection<SC1> by lazy {
        (0 until 16).map { index ->
            val x: Double
            val y: Double
            when (index) {
                7 -> {
                    x = 1.5 * Monitor.PIXEL_XY_SPACING;
                    y = 1.5 * Monitor.PIXEL_XY_SPACING;
                }
                4 -> {
                    x = 0.5 * Monitor.PIXEL_XY_SPACING;
                    y = 1.5 * Monitor.PIXEL_XY_SPACING;
                }
                6 -> {
                    x = 1.5 * Monitor.PIXEL_XY_SPACING;
                    y = 0.5 * Monitor.PIXEL_XY_SPACING;
                }
                5 -> {
                    x = 0.5 * Monitor.PIXEL_XY_SPACING;
                    y = 0.5 * Monitor.PIXEL_XY_SPACING;
                }

                3 -> {
                    x = -1.5 * Monitor.PIXEL_XY_SPACING;
                    y = 1.5 * Monitor.PIXEL_XY_SPACING;
                }
                0 -> {
                    x = -0.5 * Monitor.PIXEL_XY_SPACING;
                    y = 1.5 * Monitor.PIXEL_XY_SPACING;
                }
                2 -> {
                    x = -1.5 * Monitor.PIXEL_XY_SPACING;
                    y = 0.5 * Monitor.PIXEL_XY_SPACING;
                }
                1 -> {
                    x = -0.5 * Monitor.PIXEL_XY_SPACING;
                    y = 0.5 * Monitor.PIXEL_XY_SPACING;
                }

                11 -> {
                    x = -1.5 * Monitor.PIXEL_XY_SPACING;
                    y = -1.5 * Monitor.PIXEL_XY_SPACING;
                }
                8 -> {
                    x = -0.5 * Monitor.PIXEL_XY_SPACING;
                    y = -1.5 * Monitor.PIXEL_XY_SPACING;
                }
                10 -> {
                    x = -1.5 * Monitor.PIXEL_XY_SPACING;
                    y = -0.5 * Monitor.PIXEL_XY_SPACING;
                }
                9 -> {
                    x = -0.5 * Monitor.PIXEL_XY_SPACING;
                    y = -0.5 * Monitor.PIXEL_XY_SPACING;
                }

                15 -> {
                    x = 1.5 * Monitor.PIXEL_XY_SPACING;
                    y = -1.5 * Monitor.PIXEL_XY_SPACING;
                }
                12 -> {
                    x = 0.5 * Monitor.PIXEL_XY_SPACING;
                    y = -1.5 * Monitor.PIXEL_XY_SPACING;
                }
                14 -> {
                    x = 1.5 * Monitor.PIXEL_XY_SPACING;
                    y = -0.5 * Monitor.PIXEL_XY_SPACING;
                }
                13 -> {
                    x = 0.5 * Monitor.PIXEL_XY_SPACING;
                    y = -0.5 * Monitor.PIXEL_XY_SPACING;
                }
                else -> throw Error();
            }
            val offset = Point3D(-y, x, 0)//rotateDetector(Point3D(x, y, 0.0));
            val pixelName = "${name}_${index}"
            SC1(pixelName, center + offset)
        }
    }
}

//class Layer(val name: String, val z: Double) {
//    val detectors: Collection<SC16> by lazy {
//
//    }
//}


expect fun readResource(path: String): String

internal expect fun readMonitorConfig(): String

/**
 * General geometry definitions
 * Created by darksnake on 09-May-16.
 */
object Monitor {

    const val GEOMETRY_TOLERANCE = 0.01
    const val PIXEL_XY_SIZE = 122.0
    const val PIXEL_XY_SPACING = 123.2
    const val PIXEL_Z_SIZE = 30.0
    const val CENTRAL_LAYER_Z = 0.0
    const val UPPER_LAYER_Z = -166.0
    const val LOWER_LAYER_Z = 180.0

    /**
     * Build map for the whole monitor
     */
    val detectors: Collection<SC16> by lazy {
        readMonitorConfig()
            .lineSequence()
            .mapNotNull { line ->
                if (line.startsWith(" ")) {
                    val split = line.trim().split("\\s+".toRegex())
                    val detectorName = split[1];
                    val x = split[4].toDouble() - 500
                    val y = split[5].toDouble() - 500
                    val z = 180 - split[6].toDouble()
                    SC16(detectorName, Point3D(x, y, z))
                } else {
                    null
                }
            }.toList()
    }

    val pixels: Collection<SC1> get() = detectors.flatMap { it.pixels }

}