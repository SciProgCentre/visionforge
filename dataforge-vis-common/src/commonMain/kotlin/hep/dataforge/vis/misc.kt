package hep.dataforge.vis

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