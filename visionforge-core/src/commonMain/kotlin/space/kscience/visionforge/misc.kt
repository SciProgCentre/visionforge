package space.kscience.visionforge

import space.kscience.dataforge.meta.Laminate
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.isLeaf

@DslMarker
public annotation class VisionBuilder

public fun List<Meta?>.merge(): Meta? {
    val first = firstOrNull { it != null }
    return when {
        first == null -> null
        first.isLeaf -> first //fast search for first entry if it is value
        else -> Laminate(filterNotNull()) //merge nodes if first encountered node is meta
    }
}