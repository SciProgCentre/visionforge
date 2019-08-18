package hep.dataforge.vis.common

import hep.dataforge.meta.MetaBuilder
import hep.dataforge.meta.MetaItem
import hep.dataforge.names.Name
import hep.dataforge.names.NameToken
import hep.dataforge.names.asName
import hep.dataforge.names.isEmpty
import kotlinx.serialization.Transient


/**
 * Abstract implementation of group of [VisualObject]
 */
abstract class AbstractVisualGroup : AbstractVisualObject(), VisualGroup {

    //protected abstract val _children: MutableMap<NameToken, T>

    /**
     * A map of top level named children
     */
    abstract override val children: Map<NameToken, VisualObject> //get() = _children

    override fun propertyChanged(name: Name, before: MetaItem<*>?, after: MetaItem<*>?) {
        super.propertyChanged(name, before, after)
        forEach {
            it.propertyChanged(name, before, after)
        }
    }

    private data class Listener(val owner: Any?, val callback: (Name, VisualObject?) -> Unit)

    @Transient
    private val listeners = HashSet<Listener>()

    /**
     * Add listener for children change
     */
    override fun onChildrenChange(owner: Any?, action: (Name, VisualObject?) -> Unit) {
        listeners.add(Listener(owner, action))
    }

    /**
     * Remove children change listener
     */
    override fun removeChildrenChangeListener(owner: Any?) {
        listeners.removeAll { it.owner === owner }
    }

//    /**
//     * Propagate children change event upwards
//     */
//    protected fun childrenChanged(name: Name, child: VisualObject?) {
//
//    }

    protected abstract fun removeChild(token: NameToken)

    protected abstract fun setChild(token: NameToken, child: VisualObject?)

    protected abstract fun createGroup(name: Name): VisualGroup

    /**
     * Add named or unnamed child to the group. If key is [null] the child is considered unnamed. Both key and value are not
     * allowed to be null in the same time. If name is present and [child] is null, the appropriate element is removed.
     */
    override fun set(name: Name, child: VisualObject?) {
        when {
            name.isEmpty() -> error("")
            name.length == 1 -> {
                val token = name.first()!!
                if (child == null) {
                    removeChild(token)
                } else {
                    if (child.parent == null) {
                        child.parent = this
                    } else {
                        error("Can't reassign existing parent for $child")
                    }
                    setChild(token, child)
                }
            }
            else -> {
                //TODO add safety check
                val parent = (get(name.cutLast()) as? VisualGroup) ?: createGroup(name.cutLast())
                parent[name.last()!!.asName()] = child
            }
        }
        listeners.forEach { it.callback(name, child) }
    }

    operator fun set(key: String, child: VisualObject?) = set(key.asName(), child)

    protected fun MetaBuilder.updateChildren() {
        //adding named children
        children.forEach {
            "children[${it.key}]" to it.value.toMeta()
        }
    }

    override fun MetaBuilder.updateMeta() {
        updateChildren()
    }
}