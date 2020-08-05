package hep.dataforge.vis

import hep.dataforge.names.*
import hep.dataforge.provider.Provider

/**
 * Represents a group of [VisualObject] instances
 */
interface VisualGroup : Provider, VisualObject {
    /**
     * A map of top level named children
     */
    val children: Map<NameToken, VisualObject>

    override val defaultTarget: String get() = VisualObject.TYPE

    /**
     * A stylesheet for this group and its descendants. Stylesheet is not applied directly,
     * but instead is just a repository for named configutations
     */
    val styleSheet: StyleSheet?

    /**
     * A map of direct children for specific target
     * (currently "visual" or "style")
     */
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
        children.values.forEach {
            it.parent = this
            (it as? VisualGroup)?.attachChildren()
        }
    }

    companion object {
        const val STYLE_TARGET = "style"
    }
}

/**
 * Iterate over children of this group
 */
operator fun VisualGroup.iterator(): Iterator<VisualObject> = children.values.iterator()

val VisualGroup.isEmpty: Boolean get() = this.children.isEmpty()

/**
 * Mutable version of [VisualGroup]
 */
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

operator fun VisualGroup.get(str: String?): VisualObject? = get(str?.toName() ?: Name.EMPTY)

operator fun MutableVisualGroup.set(key: String, child: VisualObject?) {
    set(key.toName(), child)
}

fun MutableVisualGroup.removeAll() = children.keys.map { it.asName() }.forEach { this[it] = null }