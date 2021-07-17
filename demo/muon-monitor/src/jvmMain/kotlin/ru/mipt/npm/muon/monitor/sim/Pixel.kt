package ru.mipt.npm.muon.monitor.sim

import org.apache.commons.math3.geometry.euclidean.threed.Line
import org.apache.commons.math3.geometry.euclidean.threed.Plane
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D
import ru.mipt.npm.muon.monitor.Monitor.CENTRAL_LAYER_Z
import ru.mipt.npm.muon.monitor.Monitor.GEOMETRY_TOLERANCE
import ru.mipt.npm.muon.monitor.Monitor.LOWER_LAYER_Z
import ru.mipt.npm.muon.monitor.Monitor.PIXEL_XY_SIZE
import ru.mipt.npm.muon.monitor.Monitor.UPPER_LAYER_Z
import ru.mipt.npm.muon.monitor.SC1
import kotlin.random.Random

/**
 * Auxiliary cache for [SC1] planes
 */
internal class SC1Aux(val sc: SC1, var efficiency: Double = 1.0) {
    //    val layer: Layer = findLayer(center.z);
    private val upLayer =
        findLayer(sc.center.z + sc.zSize / 2f)//Layer("${name}_up", center.z + zSize / 2.0);
    private val bottomLayer =
        findLayer(sc.center.z - sc.zSize / 2f)//Layer("${name}_bottom", center.z - zSize / 2.0);
    private val centralLayer = findLayer(sc.center.z)

    private val center = Vector3D(sc.center.x.toDouble(), sc.center.y.toDouble(), sc.center.z.toDouble())

    private val sideLayers: Array<Plane> = arrayOf(
        Plane(center.add(Vector3D(PIXEL_XY_SIZE / 2.0, 0.0, 0.0)), Vector3D(1.0, 0.0, 0.0), GEOMETRY_TOLERANCE),
        Plane(center.add(Vector3D(-PIXEL_XY_SIZE / 2.0, 0.0, 0.0)), Vector3D(-1.0, 0.0, 0.0), GEOMETRY_TOLERANCE),
        Plane(center.add(Vector3D(0.0, PIXEL_XY_SIZE / 2.0, 0.0)), Vector3D(0.0, 1.0, 0.0), GEOMETRY_TOLERANCE),
        Plane(center.add(Vector3D(0.0, -PIXEL_XY_SIZE / 2.0, 0.0)), Vector3D(0.0, -1.0, 0.0), GEOMETRY_TOLERANCE)
    )

    //TODO add efficiency
    private fun containsPoint(x: Double, y: Double, z: Double, tolerance: Double = GEOMETRY_TOLERANCE): Boolean {
        return x <= sc.center.x + sc.xSize / 2.0 + tolerance && x >= sc.center.x - sc.xSize / 2.0 - tolerance &&
                y <= sc.center.y + sc.ySize / 2.0 + tolerance && y >= sc.center.y - sc.ySize / 2.0 - tolerance &&
                z <= sc.center.z + sc.zSize / 2.0 + tolerance && z >= sc.center.z - sc.zSize / 2.0 - tolerance;
    }

    /**
     * Check if pixel contains point
     */
    private fun containsPoint(point: Vector3D, tolerance: Double = GEOMETRY_TOLERANCE): Boolean {
        return containsPoint(point.x, point.y, point.z, tolerance);
    }

    /**
     * Return number of detector pixel like SCxx-12
     */
    fun getDetectorNumber(): Int {
        return sc.name.substring(2, 4).toInt();
    }

    /**
     * Return number of pixel in detector like SC79-xx
     */
    fun getPixelNumber(): Int {
        return sc.name.substring(5).toInt();
    }

    /**
     * The layer number from up to bottom
     */
    fun getLayerNumber(): Int {
        return when (this.center.z.toFloat()) {
            UPPER_LAYER_Z -> 1
            CENTRAL_LAYER_Z -> 2;
            LOWER_LAYER_Z -> 3;
            else -> throw RuntimeException("Unknown layer");
        }
    }

    /**
     * Check if track crosses the pixel
     */
    fun isHit(track: Line): Boolean {
        //check central plane as well as upper and bottom planes of the layer
        val upperIntersection = upLayer.intersection(track);
        val bottomIntersection = bottomLayer.intersection(track);
        val upperHit = containsPoint(upperIntersection);
        val bottomHit = containsPoint(bottomIntersection);

        if (!bottomHit && !upperHit) {
            return false;
        } else if (upperHit && bottomHit) {
            return eff();
        } else {
            val verticalHitPoint = if (upperHit) {
                upperIntersection
            } else {
                bottomIntersection
            }
            val horizontalHitPoint = getHorizontalHitPoint(track);
            return if (horizontalHitPoint == null) {
                //If horizontal intersection could not be found, it is near the rib and therefore length is always sufficient
                true;
            } else {
                val length = verticalHitPoint.distance(horizontalHitPoint);
                (length >= MINIMAL_TRACK_LENGTH) && eff();
            }
        }
    }

    private fun getHorizontalHitPoint(track: Line): Vector3D? {
        return sideLayers
            .map { it.intersection(track) }//FIXME there is a problem with geometric tolerances here
            .firstOrNull { it != null && containsPoint(it, 100 * GEOMETRY_TOLERANCE) };
    }

    private fun eff(): Boolean {
        return efficiency == 1.0 || Random.nextDouble() < efficiency;
    }
}

private val auxCache = HashMap<SC1, SC1Aux>()

fun SC1.isHit(track: Line): Boolean{
    return auxCache.getOrPut(this){
        SC1Aux(this)
    }.isHit(track)
}