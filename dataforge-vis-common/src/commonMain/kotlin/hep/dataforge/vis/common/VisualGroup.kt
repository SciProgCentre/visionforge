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
class VisualGroup(
    override val parent: VisualObject? = null, meta: Meta = EmptyMeta
) : VisualObject, Iterable<VisualObject>, Provider {

    private val namedChildren = HashMap<Name, VisualObject>()
    private val unnamedChildren = ArrayList<VisualObject>()

    override val defaultTarget: String get() = VisualObject.TYPE
    override val properties: Styled = Styled(meta)

    override fun iterator(): Iterator<VisualObject> = (namedChildren.values + unnamedChildren).iterator()

    override fun provideTop(target: String): Map<Name, Any> {
        return when(target){
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
     *
     */
    operator fun set(key: String?, child: VisualObject?) {
        if(key == null){

        } else {
            val name = key.toName()
            if (child == null) {
                namedChildren.remove(name)
            } else {
                namedChildren[name] = child
            }
            listeners.forEach { it.callback(name, child) }
        }
    }

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