package hep.dataforge.vision

import hep.dataforge.meta.*
import hep.dataforge.values.asValue

@DslMarker
public annotation class VisionBuilder

public fun Sequence<MetaItem<*>?>.merge(): MetaItem<*>? {
    return when (val first = firstOrNull { it != null }) {
        null -> null
        is MetaItem.ValueItem -> first //fast search for first entry if it is value
        is MetaItem.NodeItem -> {
            //merge nodes if first encountered node is meta
            val laminate: Laminate = Laminate(mapNotNull { it.node }.toList())
            MetaItem.NodeItem(laminate)
        }
    }
}

@DFExperimental
public val Vision.properties: Config?
    get() = (this as? VisionBase)?.properties

/**
 * Control visibility of the element
 */
public var Vision.visible: Boolean?
    get() = getProperty(Vision.VISIBLE_KEY).boolean
    set(value) = setProperty(Vision.VISIBLE_KEY, value?.asValue())

public fun Vision.configure(meta: Meta?): Unit = update(VisionChange(properties = meta))

public fun Vision.configure(block: MutableMeta<*>.() -> Unit): Unit = configure(Meta(block))