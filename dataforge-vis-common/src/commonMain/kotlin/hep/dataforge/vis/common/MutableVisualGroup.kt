package hep.dataforge.vis.common

import hep.dataforge.meta.Meta
import hep.dataforge.names.*
import hep.dataforge.provider.Provider

interface VisualGroup : Provider, Iterable<VisualObject>, VisualObject {
    /**
     * A map of top level named children
     */
    val children: Map<NameToken, VisualObject>

    override val defaultTarget: String get() = VisualObject.TYPE

    override fun provideTop(target: String): Map<Name, Any> =
        when (target) {
            VisualObject.TYPE -> children.flatMap { (key, value) ->
                val res: Map<Name, Any> = if (value is VisualGroup) {
                    value.provideTop(target).mapKeys { key + it.key }
                } else {
                    mapOf(key.asName() to value)
                }
                res.entries
            }.associate { it.toPair() }
            //TODO add styles
            else -> emptyMap()
        }


    /**
     * Iterate over children of this group
     */
    override fun iterator(): Iterator<VisualObject> = children.values.iterator()

    /**
     * Resolve style by its name
     * TODO change to Config?
     */
    fun getStyle(name: Name): Meta?

    /**
     * Add or replace style with given name
     */
    fun addStyle(name: Name, meta: Meta, apply: Boolean = true)

    operator fun get(name: Name): VisualObject? {
        return when {
            name.isEmpty() -> this
            name.length == 1 -> children[name.first()!!]
            else -> (children[name.first()!!] as? VisualGroup)?.get(name.cutFirst())
        }
    }
}

interface MutableVisualGroup : VisualGroup {

    /**
     * Add listener for children structure change.
     * @param owner the handler to properly remove listeners
     * @param action First argument of the action is the name of changed child. Second argument is the new value of the object.
     */
    fun onChildrenChange(owner: Any?, action: (Name, VisualObject?) -> Unit)

    /**
     * Remove children change listener
     */
    fun removeChildrenChangeListener(owner: Any?)

    operator fun set(name: Name, child: VisualObject?)
}

operator fun VisualGroup.get(str: String?) = get(str?.toName() ?: EmptyName)