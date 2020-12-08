package hep.dataforge.vision

import hep.dataforge.meta.configure
import hep.dataforge.names.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient


/**
 * Abstract implementation of mutable group of [Vision]
 */
@Serializable
@SerialName("vision.group")
public open class VisionGroupBase : VisionBase(), MutableVisionGroup {

    /**
     * Internal mutable container for group children
     * TODO made protected due to [https://github.com/Kotlin/kotlinx.serialization/issues/1200]
     */
    @SerialName("children")
    protected val childrenInternal: MutableMap<NameToken, Vision> = LinkedHashMap()

    /**
     * A map of top level named children
     */
    override val children: Map<NameToken, Vision> get() = childrenInternal

    final override var styleSheet: StyleSheet? = null
        private set

    /**
     * Update or create stylesheet
     */
    public open fun styleSheet(block: StyleSheet.() -> Unit) {
        if (styleSheet == null) {
            styleSheet = StyleSheet(this@VisionGroupBase)
        }
        styleSheet!!.block()
    }

    override fun propertyChanged(name: Name) {
        super.propertyChanged(name)
        for (obj in this) {
            obj.propertyChanged(name)
        }
    }

    private data class StructureChangeListener(
        val owner: Any?,
        val callback: (token: NameToken, before: Vision?, after: Vision?) -> Unit,
    )

    @Transient
    private val structureChangeListeners = HashSet<StructureChangeListener>()

    /**
     * Add listener for children change
     */
    override fun onStructureChange(owner: Any?, action: (token: NameToken, before: Vision?, after: Vision?) -> Unit) {
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
    override fun removeStructureChangeListener(owner: Any?) {
        structureChangeListeners.removeAll { owner == null || it.owner === owner }
    }

    /**
     * Propagate children change event upwards
     */
    protected fun childrenChanged(name: NameToken, before: Vision?, after: Vision?) {
        structureChangeListeners.forEach { it.callback(name, before, after) }
    }

    /**
     * Remove a child with given name token
     */
    public fun removeChild(token: NameToken): Vision? {
        val removed = childrenInternal.remove(token)
        removed?.parent = null
        return removed
    }

    /**
     * Add a static child. Statics could not be found by name, removed or replaced. Changing statics also do not trigger events.
     */
    protected open fun addStatic(child: Vision): Unit {
        attachChild(NameToken("@static", index = child.hashCode().toString()), child)
    }

    /**
     * Create a vision group of the same type as this vision group. Do not attach it.
     */
    protected open fun createGroup(): VisionGroupBase = VisionGroupBase()

    /**
     * Set parent for given child and attach it
     */
    private fun attachChild(token: NameToken, child: Vision) {
        if (child.parent == null) {
            child.parent = this
            childrenInternal[token] = child
        } else if (child.parent !== this) {
            error("Can't reassign existing parent for $child")
        }
    }

    /**
     * Recursively create a child group
     */
    private fun createGroups(name: Name): VisionGroupBase {
        return when {
            name.isEmpty() -> error("Should be unreachable")
            name.length == 1 -> {
                val token = name.tokens.first()
                when (val current = children[token]) {
                    null -> createGroup().also { child ->
                        attachChild(token, child)
                    }
                    is VisionGroupBase -> current
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
                val before = children[token]
                if (child == null) {
                    removeChild(token)
                } else {
                    attachChild(token, child)
                }
                childrenChanged(token, before, child)
            }
            else -> {
                //TODO add safety check
                val parent = (get(name.cutLast()) as? MutableVisionGroup) ?: createGroups(name.cutLast())
                parent[name.tokens.last().asName()] = child
            }
        }
    }

    override fun update(change: VisionChange) {
        //update stylesheet
//            val changeStyleSheet = change.styleSheet
//            if (changeStyleSheet != null) {
//                styleSheet {
//                    update(changeStyleSheet)
//                }
//            }
        change.propertyChange.forEach {(childName,configChange)->
            get(childName)?.configure(configChange)
        }
        change.childrenChange.forEach { (name, child) ->
            set(name, child)
        }
        super.update(change)
    }
}