package hep.dataforge.vis

import hep.dataforge.meta.Laminate
import hep.dataforge.meta.MetaItem
import hep.dataforge.meta.node
import hep.dataforge.names.Name
import hep.dataforge.names.isEmpty

/**
 * Return nearest selectable parent [Name]
 */
tailrec fun Name.selectable(): Name? = when {
    isEmpty() -> {
        null
    }
    last()?.body?.startsWith("@") != true -> {
        this
    }
    else -> {
        cutLast().selectable()
    }
}

fun Sequence<MetaItem<*>?>.merge(): MetaItem<*>?{
    return when (val first = filterNotNull().firstOrNull()) {
        null -> null
        is MetaItem.ValueItem -> first //fast search for first entry if it is value
        is MetaItem.NodeItem -> {
            //merge nodes if first encountered node is meta
            val laminate: Laminate = Laminate(mapNotNull { it.node }.toList())
            MetaItem.NodeItem(laminate)
        }
    }
}