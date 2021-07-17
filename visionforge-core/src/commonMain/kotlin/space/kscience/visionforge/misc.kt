package space.kscience.visionforge

import space.kscience.dataforge.meta.*
import space.kscience.dataforge.values.asValue

@DslMarker
public annotation class VisionBuilder

public fun List<MetaItem?>.merge(): MetaItem? = when (val first = firstOrNull { it != null }) {
    null -> null
    is MetaItemValue -> first //fast search for first entry if it is value
    is MetaItemNode -> {
        //merge nodes if first encountered node is meta
        val laminate: Laminate = Laminate(mapNotNull { it.node })
        MetaItemNode(laminate)
    }
}

/**
 * Control visibility of the element
 */
public var Vision.visible: Boolean?
    get() = getProperty(Vision.VISIBLE_KEY).boolean
    set(value) = setProperty(Vision.VISIBLE_KEY, value?.asValue())

public fun Vision.configure(meta: Meta?): Unit = update(VisionChange(properties = meta))

public fun Vision.configure(block: MetaBuilder.() -> Unit): Unit = configure(Meta(block))