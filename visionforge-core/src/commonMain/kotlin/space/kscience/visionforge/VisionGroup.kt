package space.kscience.visionforge

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import space.kscience.dataforge.misc.DFExperimental
import space.kscience.dataforge.names.*
import space.kscience.dataforge.provider.Provider

@DslMarker
public annotation class VisionBuilder

public interface VisionContainer<out V : Vision> {
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
            STYLE_TARGET -> styleSheet.items?.mapKeys { it.key.asName() } ?: emptyMap()
            else -> emptyMap()
        }

    public override operator fun get(name: Name): Vision? {
        return when {
            name.isEmpty() -> this
            name.length == 1 -> children[name.tokens.first()]
            else -> (children[name.tokens.first()] as? VisionGroup)?.get(name.cutFirst())
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

public fun VisionGroup.isEmpty(): Boolean = this.children.isEmpty()

public interface VisionContainerBuilder<in V : Vision> {
    //TODO add documentation
    public operator fun set(name: Name?, child: V?)
}

/**
 * Mutable version of [VisionGroup]
 */
public interface MutableVisionGroup : VisionGroup, VisionContainerBuilder<Vision> {
    public fun onStructureChanged(owner: Any?, block: VisionGroup.(Name) -> Unit)

    public fun removeStructureListener(owner: Any?)
}


/**
 * Flow structure changes of this group. Unconsumed changes are discarded
 */
@OptIn(ExperimentalCoroutinesApi::class)
@DFExperimental
public val MutableVisionGroup.structureChanges: Flow<Name>
    get() = callbackFlow {
        meta.onChange(this) { name ->
            launch {
                send(name)
            }
        }
        awaitClose {
            removeStructureListener(this)
        }
    }


public operator fun <V : Vision> VisionContainer<V>.get(str: String): V? = get(Name.parse(str))

public operator fun <V : Vision> VisionContainerBuilder<V>.set(token: NameToken, child: V?): Unit =
    set(token.asName(), child)

public operator fun <V : Vision> VisionContainerBuilder<V>.set(key: String?, child: V?): Unit =
    set(key?.let(Name::parse), child)

public fun MutableVisionGroup.removeAll(): Unit = children.keys.map { it.asName() }.forEach { this[it] = null }