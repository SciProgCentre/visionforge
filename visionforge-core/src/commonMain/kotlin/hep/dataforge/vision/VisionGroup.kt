package hep.dataforge.vision

import hep.dataforge.names.*
import hep.dataforge.provider.Provider

public interface VisionContainer<out V: Vision>{
    public operator fun get(name: Name): V?
}

/**
 * Represents a group of [Vision] instances
 */
public interface VisionGroup : Provider, Vision, VisionContainer<Vision> {
    /**
     * A map of top level named children
     */
    public val children: Map<NameToken, Vision>

    override val defaultTarget: String get() = Vision.TYPE

    /**
     * A stylesheet for this group and its descendants. Stylesheet is not applied directly,
     * but instead is just a repository for named configurations.
     */
    public val styleSheet: StyleSheet?

    /**
     * A map of direct children for specific target
     * (currently "visual" or "style")
     */
    override fun content(target: String): Map<Name, Any> =
        when (target) {
            Vision.TYPE -> children.flatMap { (key, value) ->
                val res: Map<Name, Any> = if (value is VisionGroup) {
                    value.content(target).mapKeys { key + it.key }
                } else {
                    mapOf(key.asName() to value)
                }
                res.entries
            }.associate { it.toPair() }
            STYLE_TARGET -> styleSheet?.items?.mapKeys { it.key.toName() } ?: emptyMap()
            else -> emptyMap()
        }

    public override operator fun get(name: Name): Vision? {
        return when {
            name.isEmpty() -> this
            name.length == 1 -> children[name.tokens.first()]
            else -> (children[name.tokens.first()] as? VisionGroup)?.get(name.cutFirst())
        }
    }

    /**
     * A fix for serialization bug that writes all proper parents inside the tree after deserialization
     */
    public fun attachChildren() {
        styleSheet?.owner = this
        children.values.forEach {
            it.parent = this
            (it as? VisionGroup)?.attachChildren()
        }
    }

    public companion object {
        public const val STYLE_TARGET: String = "style"
    }
}

/**
 * Iterate over children of this group
 */
public operator fun VisionGroup.iterator(): Iterator<Vision> = children.values.iterator()

public val VisionGroup.isEmpty: Boolean get() = this.children.isEmpty()

public interface VisionContainerBuilder<in V: Vision>{
    public operator fun set(name: Name, child: V?)
}

/**
 * Mutable version of [VisionGroup]
 */
public interface MutableVisionGroup : VisionGroup, VisionContainerBuilder<Vision> {

    /**
     * Add listener for children structure change.
     * @param owner the handler to properly remove listeners
     * @param action First argument of the action is the name of changed child. Second argument is the new value of the object.
     */
    public fun onStructureChange(owner: Any?, action: (token: NameToken, before: Vision?, after: Vision?) -> Unit)

    /**
     * Remove children change listener
     */
    public fun removeStructureChangeListener(owner: Any?)

//    public operator fun set(name: Name, child: Vision?)
}

public operator fun <V: Vision> VisionContainer<V>.get(str: String?): V? = get(str?.toName() ?: Name.EMPTY)

public operator fun <V: Vision>  VisionContainerBuilder<V>.set(token: NameToken, child: V?): Unit = set(token.asName(), child)
public operator fun <V: Vision>  VisionContainerBuilder<V>.set(key: String, child: V?): Unit = set(key.toName(), child)

public fun MutableVisionGroup.removeAll(): Unit = children.keys.map { it.asName() }.forEach { this[it] = null }