package hep.dataforge.vision

import hep.dataforge.meta.Laminate
import hep.dataforge.meta.MetaItem
import hep.dataforge.meta.descriptors.NodeDescriptor
import hep.dataforge.meta.node
import hep.dataforge.names.Name
import hep.dataforge.values.ValueType
import hep.dataforge.values.asValue

fun Sequence<MetaItem<*>?>.merge(): MetaItem<*>? {
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

inline fun <reified E : Enum<E>> NodeDescriptor.enum(key: Name, default: E?) = value(key) {
    type(ValueType.STRING)
    default?.let {
        default(default)
    }
    allowedValues = enumValues<E>().map { it.asValue() }
}