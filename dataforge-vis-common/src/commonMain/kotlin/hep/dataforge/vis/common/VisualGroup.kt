package hep.dataforge.vis.common

import hep.dataforge.names.*
import hep.dataforge.provider.Provider

interface VisualGroup : VisualObject, Provider, Iterable<VisualObject> {
    /**
     * A map of top level named children
     */
    val children: Map<NameToken, VisualObject>

    override val defaultTarget: String get() = VisualObject.TYPE

    override fun provideTop(target: String): Map<Name, VisualObject> = if (target == VisualObject.TYPE) {
        children.flatMap { (key, value) ->
            val res: Map<Name, VisualObject> = if (value is VisualGroup) {
                value.provideTop(target).mapKeys { key + it.key }
            } else {
                mapOf(key.asName() to value)
            }
            res.entries
        }.associate { it.toPair() }
    } else {
        emptyMap()
    }

    /**
     * Iterate over children of this group
     */
    override fun iterator(): Iterator<VisualObject> = children.values.iterator()

    /**
     * Add listener for children change
     */
    fun onChildrenChange(owner: Any?, action: (Name, VisualObject?) -> Unit)

    /**
     * Remove children change listener
     */
    fun removeChildrenChangeListener(owner: Any?)

    operator fun get(name: Name): VisualObject? {
        return when {
            name.isEmpty() -> this
            name.length == 1 -> children[name.first()!!]
            else -> (children[name.first()!!] as? VisualGroup)?.get(name.cutFirst())
        }
    }

    operator fun set(name: Name, child: VisualObject?)
}

operator fun VisualGroup.get(str: String?) = get(str?.toName() ?: EmptyName)