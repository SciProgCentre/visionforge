@file:UseSerializers(Point3DSerializer::class)
package ru.mipt.npm.muon.monitor

import hep.dataforge.vis.spatial.Point3D
import hep.dataforge.vis.spatial.Point3DSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

typealias Track = List<Point3D>

/**
 *
 */
@Serializable
data class Event(val track: Track?, val hits: Collection<String>) {
    /**
     * The unique identity for given set of hits. One identity could correspond to different tracks
     */
    val id get() = hits.sorted().joinToString(separator = ", ", prefix = "[", postfix = "]");
}