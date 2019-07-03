package hep.dataforge.vis.common

import hep.dataforge.meta.EmptyMeta
import hep.dataforge.meta.Meta
import hep.dataforge.meta.Styled
import hep.dataforge.names.Name
import hep.dataforge.names.toName
import hep.dataforge.provider.Provider

/**
 * A display group which allows both named and unnamed children
 */
class DisplayGroup(
    override val parent: DisplayObject? = null, meta: Meta = EmptyMeta
) : DisplayObject, Iterable<DisplayObject>, Provider {

    private val namedChildren = HashMap<Name, DisplayObject>()
    private val unnamedChildren = ArrayList<DisplayObject>()

    override val defaultTarget: String get() = DisplayObject.TARGET
    override val properties: Styled = Styled(meta)

    override fun iterator(): Iterator<DisplayObject> = (namedChildren.values + unnamedChildren).iterator()

    override fun listNames(target: String): Sequence<Name> =
        namedChildren.keys.asSequence()

    override fun provideTop(target: String, name: Name): Any? {
        return if (target == defaultTarget) {
            namedChildren[name]
        } else {
            null
        }
    }

    private data class Listener(val owner: Any?, val callback: (Name?, DisplayObject?) -> Unit)

    private val listeners = HashSet<Listener>()

    /**
     * Add listener for children change
     */
    fun onChildrenChange(owner: Any?, action: (Name?, DisplayObject?) -> Unit) {
        listeners.add(Listener(owner, action))
    }


    /**
     * Remove children change listener
     */
    fun removeChildrenChangeListener(owner: Any?) {
        listeners.removeAll { it.owner === owner }
    }

    /**
     *
     */
    operator fun set(key: String, child: DisplayObject?) {
        val name = key.toName()
        if (child == null) {
            namedChildren.remove(name)
        } else {
            namedChildren[name] = child
        }
        listeners.forEach { it.callback(name, child) }
    }

    /**
     * Append unnamed child
     */
    fun add(child: DisplayObject) {
        unnamedChildren.add(child)
        listeners.forEach { it.callback(null, child) }
    }

    /**
     * remove unnamed child
     */
    fun remove(child: DisplayObject) {
        unnamedChildren.remove(child)
        listeners.forEach { it.callback(null, null) }
    }
}