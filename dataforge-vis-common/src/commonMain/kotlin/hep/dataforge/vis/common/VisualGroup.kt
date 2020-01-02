package hep.dataforge.vis.common

import hep.dataforge.names.*
import hep.dataforge.provider.Provider

interface VisualGroup : Provider, Iterable<VisualObject>, VisualObject {
    /**
     * A map of top level named children
     */
    val children: Map<NameToken, VisualObject>

    override val defaultTarget: String get() = VisualObject.TYPE

    val styleSheet: StyleSheet?

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
            STYLE_TARGET -> styleSheet?.items?.mapKeys { it.key.toName() } ?: emptyMap()
            else -> emptyMap()
        }


    /**
     * Iterate over children of this group
     */
    override fun iterator(): Iterator<VisualObject> = children.values.iterator()

    operator fun get(name: Name): VisualObject? {
        return when {
            name.isEmpty() -> this
            name.length == 1 -> children[name.first()!!]
            else -> (children[name.first()!!] as? VisualGroup)?.get(name.cutFirst())
        }
    }

    /**
     * A fix for serialization bug that writes all proper parents inside the tree after deserialization
     */
    fun attachChildren() {
        styleSheet?.owner = this
        this.children.values.forEach {
            it.parent = this
            (it as? VisualGroup)?.attachChildren()
        }
    }

    companion object {
        const val STYLE_TARGET = "style"
    }
}

data class StyleRef(val group: VisualGroup, val styleName: Name)

val VisualGroup.isEmpty: Boolean get() = this.children.isEmpty()


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

operator fun VisualGroup.get(str: String?) = get(str?.toName() ?: Name.EMPTY)

fun MutableVisualGroup.removeAll() = children.keys.map { it.asName() }.forEach { this[it] = null }