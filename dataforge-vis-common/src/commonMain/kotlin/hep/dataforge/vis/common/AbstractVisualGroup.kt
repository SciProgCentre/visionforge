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


//    init {
//        //Do after deserialization
//        children.values.forEach {
//            it.parent = this
//        }
//    }

    override fun propertyChanged(name: Name, before: MetaItem<*>?, after: MetaItem<*>?) {
        super.propertyChanged(name, before, after)
        forEach {
            it.propertyChanged(name, before, after)
        }
    }

    private data class StructureChangeListeners(val owner: Any?, val callback: (Name, VisualObject?) -> Unit)

    @Transient
    private val structureChangeListeners = HashSet<StructureChangeListeners>()

    /**
     * Add listener for children change
     */
    override fun onChildrenChange(owner: Any?, action: (Name, VisualObject?) -> Unit) {
        structureChangeListeners.add(StructureChangeListeners(owner, action))
    }

    /**
     * Remove children change listener
     */
    override fun removeChildrenChangeListener(owner: Any?) {
        structureChangeListeners.removeAll { it.owner === owner }
    }

    /**
     * Propagate children change event upwards
     */
    protected fun childrenChanged(name: Name, child: VisualObject?) {
        structureChangeListeners.forEach { it.callback(name, child) }
    }

    /**
     * Remove a child with given name token
     */
    protected abstract fun removeChild(token: NameToken)

    /**
     * Add, remove or replace child with given name
     */
    protected abstract fun setChild(token: NameToken, child: VisualObject)

    /**
     * Add a static child. Statics could not be found by name, removed or replaced
     */
    protected abstract fun addStatic(child: VisualObject)

    /**
     * Recursively create a child group
     */
    protected abstract fun createGroup(name: Name): VisualGroup

    /**
     * Add named or unnamed child to the group. If key is [null] the child is considered unnamed. Both key and value are not
     * allowed to be null in the same time. If name is present and [child] is null, the appropriate element is removed.
     */
    override fun set(name: Name, child: VisualObject?) {
        when {
            name.isEmpty() -> {
                if (child != null) {
                    addStatic(child)
                }
            }
            name.length == 1 -> {
                val token = name.first()!!
                if (child == null) {
                    removeChild(token)
                } else {
                    setChild(token, child)
                }
            }
            else -> {
                //TODO add safety check
                val parent = (get(name.cutLast()) as? VisualGroup) ?: createGroup(name.cutLast())
                parent[name.last()!!.asName()] = child
            }
        }
        structureChangeListeners.forEach { it.callback(name, child) }
    }

    operator fun set(key: String, child: VisualObject?) = if (key.isBlank()) {
        child?.let { addStatic(child) }
    } else {
        set(key.asName(), child)
    }

//    operator fun set(key: String?, child: VisualObject?) = set(key ?: "", child)

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