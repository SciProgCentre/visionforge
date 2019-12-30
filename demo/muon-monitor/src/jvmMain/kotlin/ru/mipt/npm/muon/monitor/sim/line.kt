package ru.mipt.npm.muon.monitor.sim

import hep.dataforge.vis.spatial.Point3D
import org.apache.commons.math3.geometry.euclidean.threed.Line
import org.apache.commons.math3.geometry.euclidean.threed.Plane
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D
import ru.mipt.npm.muon.monitor.Monitor.CENTRAL_LAYER_Z
import ru.mipt.npm.muon.monitor.Monitor.GEOMETRY_TOLERANCE

/**
 * Created by darksnake on 11-May-16.
 */

private val basePlane = Plane(Vector3D(0.0, 0.0, 0.0), Vector3D(0.0, 0.0, 1.0), GEOMETRY_TOLERANCE);

/**
 * elevation angle
 */
fun Line.getTheta(): Double {
    return direction.delta;
}

/**
 * Azimuthal angle
 */
fun Line.getPhi(): Double {
    return direction.alpha;
}

/**
 *  x of intersection with base (central) plane
 */
fun Line.getX(): Double {
    return basePlane.intersection(this).x;
}

fun Line.getY(): Double {
    return basePlane.intersection(this).y;
}

fun makeTrack(start: Vector3D, direction: Vector3D): Line {
    return Line(start, start.add(direction), GEOMETRY_TOLERANCE)
}

fun makeTrack(x: Double, y: Double, theta: Double, phi: Double): Line {
    //TODO check angle definitions
    return makeTrack(
        Vector3D(x, y, CENTRAL_LAYER_Z),
        Vector3D(phi, theta)
    )
}

fun Vector3D.toPoint() = Point3D(x, y, z)

fun Line.toPoints(): List<Point3D> {
    val basePoint = basePlane.intersection(this)
    val bottom = basePoint.subtract(300.0, direction)
    val top = basePoint.add(300.0, direction)
    return listOf(bottom.toPoint(), top.toPoint())
}

