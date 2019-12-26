package ru.mipt.npm.muon.sim

import kotlinx.serialization.Serializable
import ru.mipt.npm.muon.monitor.Line


/**
 * Created by darksnake on 11-May-16.
 */
@Serializable
data class Event(val track: Line, val hits: Set<String>) {
    /**
     * The unique identity for given set of hits. One identity could correspond to different tracks
     */
    val id get() = hits.sorted().joinToString(separator = ", ", prefix = "[", postfix = "]");
}