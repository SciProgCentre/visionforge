package hep.dataforge.vis

import hep.dataforge.meta.*
import hep.dataforge.names.Name
import hep.dataforge.names.toName
import hep.dataforge.vis.DisplayObject.Companion.DEFAULT_TYPE
import hep.dataforge.vis.DisplayObject.Companion.META_KEY
import hep.dataforge.vis.DisplayObject.Companion.TAGS_KEY

/**
 * A root type for display hierarchy
 */
interface DisplayObject {

    /**
     * The parent object of this one. If null, this one is a root.
     */
    val parent: DisplayObject?

    /**
     * The type of this object. Uses `.` notation. Empty type means untyped group
     */
    val type: String

    val properties: Styled

    companion object {
        const val DEFAULT_TYPE = ""
        const val TYPE_KEY = "@type"
        const val CHILDREN_KEY = "@children"
        const val META_KEY = "@meta"
        const val TAGS_KEY = "@tags"
    }
}

interface DisplayGroup : DisplayObject {

    val children: List<DisplayObject>

    /**
     * Add a child object and notify listeners
     */
    fun addChild(obj: DisplayObject)

    /**
     * Remove a specific child and notify listeners
     */
    fun removeChild(obj: DisplayObject)

    /**
     * Add listener for children change
     * TODO add detailed information into change listener
     */
    fun onChildrenChange(owner: Any? = null, action: () -> Unit)

    /**
     * Remove children change listener
     */
    fun removeChildrenChangeListener(owner: Any? = null)
}

/**
 * Get the property of this display object of parent's if not found
 */
tailrec operator fun DisplayObject.get(name: Name): MetaItem<*>? = properties[name] ?: parent?.get(name)

operator fun DisplayObject.get(name: String) = get(name.toName())

/**
 * A change listener for [DisplayObject] configuration.
 */
fun DisplayObject.onChange(owner: Any?, action: (Name, before: MetaItem<*>?, after: MetaItem<*>?) -> Unit) {
    properties.style.onChange(owner, action)
    parent?.onChange(owner, action)
}

/**
 * Remove all meta listeners with matching owners
 */
fun DisplayObject.removeChangeListener(owner: Any?) {
    properties.style.removeListener(owner)
    parent?.removeChangeListener(owner)
}


/**
 * Additional meta not relevant to display
 */
val DisplayObject.meta: Meta get() = properties[META_KEY]?.node ?: EmptyMeta

val DisplayObject.tags: List<String> get() = properties[TAGS_KEY].stringList

internal data class ObjectListener(
    val owner: Any?,
    val action: () -> Unit
)

/**
 * Basic group of display objects
 */
open class DisplayNode(
    override val parent: DisplayObject? = null,
    override val type: String = DEFAULT_TYPE,
    meta: Meta = EmptyMeta
) : DisplayGroup {

    private val _children = ArrayList<DisplayObject>()
    override val children: List<DisplayObject> get() = _children
    override val properties = Styled(meta)
    private val listeners = HashSet<ObjectListener>()

    override fun addChild(obj: DisplayObject) {
//        val before = _children[name]
//        if (obj == null) {
//            _children.remove(name)
//        } else {
//            _children[name] = obj
//        }
//        listeners.forEach { it.action(name, before, obj) }
        _children.add(obj)
        listeners.forEach { it.action() }
    }

    override fun removeChild(obj: DisplayObject) {
        if (_children.remove(obj)) {
            listeners.forEach { it.action }
        }
    }

    override fun onChildrenChange(owner: Any?, action: () -> Unit) {
        listeners.add(ObjectListener(owner, action))
    }


    override fun removeChildrenChangeListener(owner: Any?) {
        listeners.removeAll { it.owner === owner }
    }
}

/**
 * Basic [DisplayObject] leaf element
 */
open class DisplayLeaf(
    override val parent: DisplayObject?,
    override val type: String,
    meta: Meta = EmptyMeta
) : DisplayObject {
    final override val properties = Styled(meta)
}

