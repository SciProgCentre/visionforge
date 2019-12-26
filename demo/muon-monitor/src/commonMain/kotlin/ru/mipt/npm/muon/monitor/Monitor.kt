package ru.mipt.npm.muon.sim

import hep.dataforge.vis.spatial.Point3D
import hep.dataforge.vis.spatial.plus

/**
 * A single pixel
 */
class SC1(
    val name: String,
    val center: Point3D
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

/**
 * General geometry definitions
 * Created by darksnake on 09-May-16.
 */
object Monitor {

    const val GEOMETRY_TOLERANCE = 0.01;
    const val PIXEL_XY_SIZE = 122.0;
    const val PIXEL_XY_SPACING = 123.2;
    const val PIXEL_Z_SIZE = 30.0;
    const val CENTRAL_LAYER_Z = 0.0;
    const val UPPER_LAYER_Z = 166.0;
    const val LOWER_LAYER_Z = -180.0;

    private val monitorConfig = """
        --Place-|-SC16-|-TB-CHN-|-HB-CHN-|-X-coord-|-Y-coord-|-Z-coord-|-Theta-|-Phi
        ----------------------------------------------------------------------------
         RT100     SC86     3        0         0      1000         0     0      270
         RT100     SC87     6        1         0       500         0     0      270
         RT100     SC88     8        2         0         0         0     0      270
         RT100     SC91     9        3       500      1000         0     0      270
         RT100     SC92    10        4       500       500         0     0      270
         RT100     SC93    11        5       500         0         0     0      270
         RT100     SC94    12        6      1000      1000         0     0      270
         RT100     SC85    13        7      1000       500         0     0      270
         RT100     SC96    15        8      1000         0         0     0      270
        ###
         RT100     SC81    26       12       250       750       180     0      270
         RT100     SC82    27       11       250       250       180     0      270
         RT100     SC83    28       23       750       750       180     0      270
         RT100     SC84    29        9       750       250       180     0      270
        ###
         RT100     SC72    80       21      1000         0       346     0      270
         RT100     SC73    79       20      1000       500       346     0      270
         RT100     SC74    78       19      1000      1000       346     0      270
         RT100     SC75    77       18       500         0       346     0      270
         RT100     SC76    84       17       500       500       346     0      270
         RT100     SC77    75       16       500      1000       346     0      270
         RT100     SC78    74       15         0         0       346     0      270
         RT100     SC79    73       14         0       500       346     0      270
         RT100     SC80    72       13         0      1000       346     0      270
        STOP
    """.trimIndent()

    /**
     * Build map for the whole monitor
     */
    val detectors: Collection<SC16> by lazy {
        monitorConfig.lineSequence().mapNotNull { line ->
            if (line.startsWith(" ")) {
                val split = line.trim().split("\\s+".toRegex());
                val detectorName = split[1];
                val x = split[4].toDouble() - 500;
                val y = split[5].toDouble() - 500;
                val z = split[6].toDouble() - 180;
                SC16(detectorName, Point3D(x, y, z))
            } else {
                null
            }
        }.toList()
    }

}