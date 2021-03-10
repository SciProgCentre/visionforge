package ru.mipt.npm.muon.monitor.sim

import org.apache.commons.math3.geometry.euclidean.threed.Line
import org.apache.commons.math3.geometry.euclidean.threed.Plane
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D
import ru.mipt.npm.muon.monitor.Event
import ru.mipt.npm.muon.monitor.Monitor
import ru.mipt.npm.muon.monitor.SC1
import ru.mipt.npm.muon.monitor.readResource


// minimal track length in detector
internal const val MINIMAL_TRACK_LENGTH = 10.0


private val layerCache = HashMap<Double, Plane>()

fun findLayer(z: Double): Plane = layerCache.getOrPut(z) {
    Plane(
        Vector3D(0.0, 0.0, z), Vector3D(0.0, 0.0, 1.0),
        Monitor.GEOMETRY_TOLERANCE
    )
}

fun readEffs(): Map<String, Double> {
    val effMap = HashMap<String, Double>()
    var detectorName: String = ""
    var index: Int = 0
    readResource("Effs-MM-minhits-4.dat").lineSequence().forEach { line ->
        val trimmed = line.trim()
        if (trimmed.startsWith("SC")) {
            detectorName = trimmed.split(Regex("\\s+"))[2]
        } else if (trimmed.startsWith("pixel")) {
            index = 0
        } else if (trimmed.isNotEmpty()) {
            val eff = trimmed.split(Regex("\\s+"))[1].toDouble()
            effMap["SC${detectorName}_${index}"] = eff
            index++
        }
    }
    return effMap
}


fun buildEventByTrack(index: Int, track: Line, hitResolver: (Line) -> Collection<SC1> = defaultHitResolver): Event {
    return Event(index, track.toPoints(), hitResolver(track).map { it.name })
}

val defaultHitResolver: (Line) -> Collection<SC1> = { track: Line ->
    Monitor.pixels.filter { it.isHit(track) }.toSet()
}