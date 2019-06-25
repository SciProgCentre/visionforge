package hep.dataforge.vis.common

import hep.dataforge.names.Name
import hep.dataforge.names.NameToken

/**
 * A navigable hierarchical display node
 */
interface DisplayTree : DisplayGroup {
    operator fun get(nameToken: NameToken): DisplayObject?
}

interface MutableDisplayTree : DisplayTree {
    operator fun set(nameToken: NameToken, group: DisplayObject)
}

/**
 * Recursively get a child
 */
tailrec operator fun DisplayTree.get(name: Name): DisplayObject? = when (name.length) {
    0 -> this
    1 -> this[name[0]]
    else -> name.first()?.let { this[it] as? DisplayTree }?.get(name.cutFirst())
}


/**
 * Set given object creating intermediate empty groups if needed
 * @param name - the full name of a child
 * @param objFactory - a function that creates child object from parent (to avoid mutable parent parameter)
 */
fun MutableDisplayTree.set(name: Name, objFactory: (parent: DisplayObject) -> DisplayObject): Unit =
    when (name.length) {
        0 -> error("Can't set object with empty name")
        1 -> set(name[0], objFactory(this))
        else -> (this[name.first()!!] ?: DisplayObjectList(this)).run {
            if (this is MutableDisplayTree) {
                this.set(name.cutFirst(), objFactory)
            } else {
                error("Can't assign child to a leaf element $this")
            }
        }
    }