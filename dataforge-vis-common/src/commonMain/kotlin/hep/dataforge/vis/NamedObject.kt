package hep.dataforge.vis

import hep.dataforge.names.Name
import hep.dataforge.names.NameToken

interface NamedObject : DisplayObject {
    val name: String

    operator fun get(nameToken: NameToken): DisplayGroup?

    operator fun set(nameToken: NameToken, group: DisplayGroup)
}

/**
 * Recursively get a child
 */
tailrec operator fun NamedObject.get(name: Name): DisplayObject? = when (name.length) {
    0 -> this
    1 -> this[name[0]]
    else -> name.first()?.let { this[it] as? NamedObject }?.get(name.cutFirst())
}


/**
 * Set given object creating intermediate empty groups if needed
 * @param name - the full name of a child
 * @param objFactory - a function that creates child object from parent (to avoid mutable parent parameter)
 */
fun NamedObject.set(name: Name, objFactory: (parent: DisplayObject) -> DisplayGroup): Unit = when (name.length) {
    0 -> error("Can't set object with empty name")
    1 -> set(name[0], objFactory(this))
    else -> (this[name.first()!!] ?: DisplayNode(this))
        .run {
            if (this is NamedObject) {
                this.set(name.cutFirst(), objFactory)
            } else {
                error("Can't assign child to a leaf element $this")
            }
        }
}