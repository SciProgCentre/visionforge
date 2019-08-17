package hep.dataforge.vis.common

import hep.dataforge.meta.Config
import hep.dataforge.meta.MetaBuilder
import hep.dataforge.meta.MetaItem
import hep.dataforge.names.Name
import hep.dataforge.names.toName
import hep.dataforge.provider.Provider
import kotlinx.serialization.Transient
import kotlin.collections.set

open class VisualGroup<T : VisualObject> : AbstractVisualObject(), Iterable<T>, Provider {

    protected open val namedChildren: MutableMap<Name, T> = HashMap()
    protected open val unnamedChildren: MutableList<T> = ArrayList()

    override var properties: Config? = null

    override val defaultTarget: String get() = VisualObject.TYPE

    override fun iterator(): Iterator<T> = (namedChildren.values + unnamedChildren).iterator()

    override fun provideTop(target: String): Map<Name, Any> {
        return when (target) {
            VisualObject.TYPE -> namedChildren
            else -> emptyMap()
        }
    }

    override fun propertyChanged(name: Name, before: MetaItem<*>?, after: MetaItem<*>?) {
        super.propertyChanged(name, before, after)
        forEach {
            it.propertyChanged(name, before, after)
        }
    }

    private data class Listener<T : VisualObject>(val owner: Any?, val callback: (Name?, T?) -> Unit)

    @Transient
    private val listeners = HashSet<Listener<T>>()

    /**
     * Add listener for children change
     */
    fun onChildrenChange(owner: Any?, action: (Name?, T?) -> Unit) {
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
    operator fun set(name: Name?, child: T?) {
        when {
            name != null -> {
                if (child == null) {
                    namedChildren.remove(name)
                } else {
                    if (child.parent == null) {
                        child.parent = this
                    } else {
                        error("Can't reassign existing parent for $child")
                    }
                    namedChildren[name] = child
                }
                listeners.forEach { it.callback(name, child) }
            }
            child != null -> add(child)
            else -> error("Both key and child element are empty")
        }
    }

    operator fun set(key: String?, child: T?) = set(key?.asName(), child)

    /**
     * Get named child by name
     */
    operator fun get(name: Name): T? = namedChildren[name]

    /**
     * Get named child by string
     */
    operator fun get(key: String): T? = namedChildren[key.toName()]

    /**
     * Get an unnamed child
     */
    operator fun get(index: Int): T? = unnamedChildren[index]

    /**
     * Append unnamed child
     */
    fun add(child: T) {
        if (child.parent == null) {
            child.parent = this
        } else {
            error("Can't reassign existing parent for $child")
        }
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

    protected fun MetaBuilder.updateChildren() {
        //adding unnamed children
        "unnamedChildren" to unnamedChildren.map { it.toMeta() }
        //adding named children
        namedChildren.forEach {
            "children[${it.key}]" to it.value.toMeta()
        }
    }

    override fun MetaBuilder.updateMeta() {
        updateChildren()
    }
}