package hep.dataforge.vision

import hep.dataforge.names.*
import hep.dataforge.provider.Provider

/**
 * Represents a group of [Vision] instances
 */
interface VisionGroup : Provider, Vision {
    /**
     * A map of top level named children
     */
    val children: Map<NameToken, Vision>

    override val defaultTarget: String get() = Vision.TYPE

    /**
     * A stylesheet for this group and its descendants. Stylesheet is not applied directly,
     * but instead is just a repository for named configurations.
     */
    val styleSheet: StyleSheet?

    /**
     * A map of direct children for specific target
     * (currently "visual" or "style")
     */
    override fun provideTop(target: String): Map<Name, Any> =
        when (target) {
            Vision.TYPE -> children.flatMap { (key, value) ->
                val res: Map<Name, Any> = if (value is VisionGroup) {
                    value.provideTop(target).mapKeys { key + it.key }
                } else {
                    mapOf(key.asName() to value)
                }
                res.entries
            }.associate { it.toPair() }
            STYLE_TARGET -> styleSheet?.items?.mapKeys { it.key.toName() } ?: emptyMap()
            else -> emptyMap()
        }

    operator fun get(name: Name): Vision? {
        return when {
            name.isEmpty() -> this
            name.length == 1 -> children[name.first()!!]
            else -> (children[name.first()!!] as? VisionGroup)?.get(name.cutFirst())
        }
    }

    /**
     * A fix for serialization bug that writes all proper parents inside the tree after deserialization
     */
    fun attachChildren() {
        styleSheet?.owner = this
        children.values.forEach {
            it.parent = this
            (it as? VisionGroup)?.attachChildren()
        }
    }

    companion object {
        const val STYLE_TARGET = "style"
    }
}

/**
 * Iterate over children of this group
 */
operator fun VisionGroup.iterator(): Iterator<Vision> = children.values.iterator()

val VisionGroup.isEmpty: Boolean get() = this.children.isEmpty()

/**
 * Mutable version of [VisionGroup]
 */
interface MutableVisionGroup : VisionGroup {

    /**
     * Add listener for children structure change.
     * @param owner the handler to properly remove listeners
     * @param action First argument of the action is the name of changed child. Second argument is the new value of the object.
     */
    fun onChildrenChange(owner: Any?, action: (Name, Vision?) -> Unit)

    /**
     * Remove children change listener
     */
    fun removeChildrenChangeListener(owner: Any?)

    operator fun set(name: Name, child: Vision?)
}

operator fun VisionGroup.get(str: String?): Vision? = get(str?.toName() ?: Name.EMPTY)

operator fun MutableVisionGroup.set(key: String, child: Vision?) {
    set(key.toName(), child)
}

fun MutableVisionGroup.removeAll() = children.keys.map { it.asName() }.forEach { this[it] = null }