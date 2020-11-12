package hep.dataforge.vision

import hep.dataforge.names.*
import kotlinx.serialization.Transient


/**
 * Abstract implementation of mutable group of [Vision]
 */
public abstract class AbstractVisionGroup : VisionBase(), MutableVisionGroup {

    //protected abstract val _children: MutableMap<NameToken, T>

    /**
     * A map of top level named children
     */
    abstract override val children: Map<NameToken, Vision>

    final override var styleSheet: StyleSheet? = null
        private set

    /**
     * Update or create stylesheet
     */
    public open fun styleSheet(block: StyleSheet.() -> Unit) {
        if (styleSheet == null) {
            styleSheet = StyleSheet(this@AbstractVisionGroup)
        }
        styleSheet!!.block()
    }

    override fun propertyChanged(name: Name) {
        super.propertyChanged(name)
        for (obj in this) {
            obj.propertyChanged(name)
        }
    }

    private data class StructureChangeListener(val owner: Any?, val callback: (NameToken, Vision?) -> Unit)

    @Transient
    private val structureChangeListeners = HashSet<StructureChangeListener>()

    /**
     * Add listener for children change
     */
    override fun onChildrenChange(owner: Any?, action: (NameToken, Vision?) -> Unit) {
        structureChangeListeners.add(
            StructureChangeListener(
                owner,
                action
            )
        )
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
    protected fun childrenChanged(name: NameToken, child: Vision?) {
        structureChangeListeners.forEach { it.callback(name, child) }
    }

    /**
     * Remove a child with given name token
     */
    protected abstract fun removeChild(token: NameToken)

    /**
     * Add, remove or replace child with given name
     */
    protected abstract fun setChild(token: NameToken, child: Vision)

    /**
     * Add a static child. Statics could not be found by name, removed or replaced. Changing statics also do not trigger events.
     */
    protected open fun addStatic(child: Vision): Unit {
        attach(NameToken("@static", index = child.hashCode().toString()), child)
    }

    protected abstract fun createGroup(): AbstractVisionGroup

    /**
     * Set parent for given child and attach it
     */
    protected fun attach(token: NameToken, child: Vision) {
        if (child.parent == null) {
            child.parent = this
            setChild(token, child)
        } else if (child.parent !== this) {
            error("Can't reassign existing parent for $child")
        }
    }

    /**
     * Recursively create a child group
     */
    private fun createGroups(name: Name): AbstractVisionGroup {
        return when {
            name.isEmpty() -> error("Should be unreachable")
            name.length == 1 -> {
                val token = name.tokens.first()
                when (val current = children[token]) {
                    null -> createGroup().also { child ->
                        attach(token, child)
                    }
                    is AbstractVisionGroup -> current
                    else -> error("Can't create group with name $name because it exists and not a group")
                }
            }
            else -> createGroups(name.tokens.first().asName()).createGroups(name.cutFirst())
        }
    }

    /**
     * Add named or unnamed child to the group. If key is null the child is considered unnamed. Both key and value are not
     * allowed to be null in the same time. If name is present and [child] is null, the appropriate element is removed.
     */
    override fun set(name: Name, child: Vision?): Unit {
        when {
            name.isEmpty() -> {
                if (child != null) {
                    addStatic(child)
                }
            }
            name.length == 1 -> {
                val token = name.tokens.first()
                if (child == null) {
                    removeChild(token)
                } else {
                    attach(token, child)
                }
                childrenChanged(token, child)
            }
            else -> {
                //TODO add safety check
                val parent = (get(name.cutLast()) as? MutableVisionGroup) ?: createGroups(name.cutLast())
                parent[name.tokens.last().asName()] = child
            }
        }
    }

    override fun update(change: Vision) {
        if (change is VisionGroup) {
            //update stylesheet
            val changeStyleSheet = change.styleSheet
            if (changeStyleSheet != null) {
                styleSheet {
                    update(changeStyleSheet)
                }
            }
            change.children.forEach { (token, child) ->
                when {
                    child is NullVision -> removeChild(token)
                    children.containsKey(token) -> children[token]!!.update(child)
                    else -> attach(token, child)
                }
            }
        }
        super.update(change)
    }
}