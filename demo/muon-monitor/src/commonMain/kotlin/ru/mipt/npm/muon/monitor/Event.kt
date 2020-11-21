package ru.mipt.npm.muon.monitor

import hep.dataforge.vision.solid.Point3D
import kotlinx.serialization.Serializable

typealias Track = List<Point3D>

/**
 *
 */
@Serializable
data class Event(val id: Int, val track: Track?, val hits: Collection<String>) {
//    /**
//     * The unique identity for given set of hits. One identity could correspond to different tracks
//     */
//    val id get() = hits.sorted().joinToString(separator = ", ", prefix = "[", postfix = "]")
}