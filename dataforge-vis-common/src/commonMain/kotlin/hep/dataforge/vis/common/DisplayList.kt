package hep.dataforge.vis.common

import hep.dataforge.meta.EmptyMeta
import hep.dataforge.meta.Meta
import hep.dataforge.meta.Styled

internal data class InvalidationListener(
    val owner: Any?,
    val action: () -> Unit
)

/**
 * A [DisplayGroup] containing ordered list of elements
 */
class DisplayObjectList(
    override val parent: DisplayObject? = null,
//    override val type: String = DisplayObject.DEFAULT_TYPE,
    meta: Meta = EmptyMeta
) : DisplayGroup {
    private val _children = ArrayList<DisplayObject>()

    /**
     * An ordered list of direct descendants
     */
    val children: List<DisplayObject> get() = _children

    override fun iterator(): Iterator<DisplayObject>  = children.iterator()


    override val properties = Styled(meta)
    private val listeners = HashSet<InvalidationListener>()

    /**
     * Add a child object and notify listeners
     */
    fun addChild(obj: DisplayObject) {
        _children.add(obj)
        listeners.forEach { it.action() }
    }


    /**
     * Remove a specific child and notify listeners
     */
    fun removeChild(obj: DisplayObject) {
        if (_children.remove(obj)) {
            listeners.forEach { it.action }
        }
    }

    /**
     * Add listener for children change
     * TODO add detailed information into change listener
     */
    fun onChildrenChange(owner: Any?, action: () -> Unit) {
        listeners.add(InvalidationListener(owner, action))
    }


    /**
     * Remove children change listener
     */
    fun removeChildrenChangeListener(owner: Any?) {
        listeners.removeAll { it.owner === owner }
    }
}

