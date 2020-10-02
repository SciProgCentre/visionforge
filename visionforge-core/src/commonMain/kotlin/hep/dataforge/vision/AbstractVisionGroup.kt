package hep.dataforge.vision

import hep.dataforge.names.*
import kotlinx.serialization.Transient


/**
 * Abstract implementation of mutable group of [Vision]
 */
abstract class AbstractVisionGroup : AbstractVision(), MutableVisionGroup {

    //protected abstract val _children: MutableMap<NameToken, T>

    /**
     * A map of top level named children
     */
    abstract override val children: Map<NameToken, Vision>

    abstract override var styleSheet: StyleSheet?
        protected set

    /**
     * Update or create stylesheet
     */
    fun styleSheet(block: StyleSheet.() -> Unit) {
        val res = styleSheet ?: StyleSheet(this).also { styleSheet = it }
        res.block()
    }

    override fun propertyChanged(name: Name) {
        super.propertyChanged(name)
        for(obj in this) {
            obj.propertyChanged(name)
        }
    }

    // TODO Consider renaming to `StructureChangeListener` (singular)
    private data class StructureChangeListeners(val owner: Any?, val callback: (Name, Vision?) -> Unit)

    @Transient
    private val structureChangeListeners = HashSet<StructureChangeListeners>()

    /**
     * Add listener for children change
     */
    override fun onChildrenChange(owner: Any?, action: (Name, Vision?) -> Unit) {
        structureChangeListeners.add(
            StructureChangeListeners(
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
    protected fun childrenChanged(name: Name, child: Vision?) {
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
     * Add a static child. Statics could not be found by name, removed or replaced
     */
    protected open fun addStatic(child: Vision) =
        set(NameToken("@static(${child.hashCode()})").asName(), child)

    protected abstract fun createGroup(): AbstractVisionGroup

    /**
     * Set this node as parent for given node
     */
    protected fun attach(child: Vision) {
        if (child.parent == null) {
            child.parent = this
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
                        attach(child)
                        setChild(token, child)
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
                    attach(child)
                    setChild(token, child)
                }
            }
            else -> {
                //TODO add safety check
                val parent = (get(name.cutLast()) as? MutableVisionGroup) ?: createGroups(name.cutLast())
                parent[name.tokens.last().asName()] = child
            }
        }
        childrenChanged(name, child)
    }

}