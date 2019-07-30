package hep.dataforge.vis.common

import hep.dataforge.meta.Config
import hep.dataforge.meta.Laminate
import hep.dataforge.meta.Meta
import hep.dataforge.names.Name
import hep.dataforge.provider.Provider
import kotlin.collections.set

/**
 * A display group which allows both named and unnamed children
 */
open class VisualGroup(
    override val parent: VisualObject? = null, tagRefs: Array<out Meta> = emptyArray()
) : VisualObject, Iterable<VisualObject>, Provider {

    private val namedChildren = HashMap<Name, VisualObject>()
    private val unnamedChildren = ArrayList<VisualObject>()

    override val defaultTarget: String get() = VisualObject.TYPE
    override val config = Config()

    override val properties: Laminate by lazy {
        combineProperties(parent, config, tagRefs)
    }

    override fun iterator(): Iterator<VisualObject> = (namedChildren.values + unnamedChildren).iterator()

    override fun provideTop(target: String): Map<Name, Any> {
        return when (target) {
            VisualObject.TYPE -> namedChildren
            else -> emptyMap()
        }
    }

    private data class Listener(val owner: Any?, val callback: (Name?, VisualObject?) -> Unit)

    private val listeners = HashSet<Listener>()

    /**
     * Add listener for children change
     */
    fun onChildrenChange(owner: Any?, action: (Name?, VisualObject?) -> Unit) {
        listeners.add(Listener(owner, action))
    }


    /**
     * Remove children change listener
     */
    fun removeChildrenChangeListener(owner: Any?) {
        listeners.removeAll { it.owner === owner }
    }

    /**
     * Add named or unnamed child to the group. If key is [null] the child is considered unnamed. Both key and value are not
     * allowed to be null in the same time. If name is present and [child] is null, the appropriate element is removed.
     */
    operator fun set(name: Name?, child: VisualObject?) {
        when {
            name != null -> {
                if (child == null) {
                    namedChildren.remove(name)
                } else {
                    namedChildren[name] = child
                }
                listeners.forEach { it.callback(name, child) }
            }
            child != null -> unnamedChildren.add(child)
            else -> error("Both key and child element are empty")
        }
    }

    operator fun set(key: String?, child: VisualObject?) = set(key?.asName(), child)

    /**
     * Append unnamed child
     */
    fun add(child: VisualObject) {
        unnamedChildren.add(child)
        listeners.forEach { it.callback(null, child) }
    }

    /**
     * remove unnamed child
     */
    fun remove(child: VisualObject) {
        unnamedChildren.remove(child)
        listeners.forEach { it.callback(null, null) }
    }
}