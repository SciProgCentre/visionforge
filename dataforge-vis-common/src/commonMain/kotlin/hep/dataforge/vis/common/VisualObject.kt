package hep.dataforge.vis.common

import hep.dataforge.meta.*
import hep.dataforge.names.Name
import hep.dataforge.names.get
import hep.dataforge.provider.Provider
import hep.dataforge.provider.Type
import hep.dataforge.vis.common.VisualObject.Companion.TYPE
import kotlin.collections.set

private fun Laminate.withTop(meta: Meta): Laminate = Laminate(listOf(meta) + layers)
private fun Laminate.withBottom(meta: Meta): Laminate = Laminate(layers + meta)

/**
 * A root type for display hierarchy
 */
@Type(TYPE)
interface VisualObject : MetaRepr, Configurable {

    val type: String get() = this::class.simpleName ?: TYPE

    /**
     * The parent object of this one. If null, this one is a root.
     */
    val parent: VisualObject?

    /**
     * Set property for this object
     */
    fun setProperty(name: Name, value: Any?)

    /**
     * Get property including or excluding parent properties
     */
    fun getProperty(name: Name, inherit: Boolean = true): MetaItem<*>?

    /**
     * Manually trigger property changed event. If [name] is empty, notify that the whole object is changed
     */
    fun propertyChanged(name: Name, before: MetaItem<*>? = null, after: MetaItem<*>? = null): Unit

    /**
     * Add listener triggering on property change
     */
    fun onPropertyChange(owner: Any?, action: (Name, before: MetaItem<*>?, after: MetaItem<*>?) -> Unit): Unit

    /**
     * Remove change listeners with given owner.
     */
    fun removeChangeListener(owner: Any?)

    companion object {
        const val TYPE = "visual"

        //const val META_KEY = "@meta"
        //const val TAGS_KEY = "@tags"
    }
}

internal data class MetaListener(
    val owner: Any? = null,
    val action: (name: Name, oldItem: MetaItem<*>?, newItem: MetaItem<*>?) -> Unit
)


abstract class AbstractVisualObject(override val parent: VisualObject?) : VisualObject {
    private val listeners = HashSet<MetaListener>()

    override fun propertyChanged(name: Name, before: MetaItem<*>?, after: MetaItem<*>?) {
        for (l in listeners) {
            l.action(name, before, after)
        }
    }

    override fun onPropertyChange(owner: Any?, action: (Name, before: MetaItem<*>?, after: MetaItem<*>?) -> Unit) {
        listeners.add(MetaListener(owner, action))
    }

    override fun removeChangeListener(owner: Any?) {
        listeners.removeAll { it.owner == owner }
    }

    private var _config: Config? = null
    override val config: Config get() = _config ?: Config().also { _config = it }

    override fun setProperty(name: Name, value: Any?) {
        config[name] = value
    }

    override fun getProperty(name: Name, inherit: Boolean): MetaItem<*>? {
        return if (inherit) {
            _config?.get(name) ?: parent?.getProperty(name, inherit)
        } else {
            _config?.get(name)
        }
    }

    protected open fun MetaBuilder.updateMeta() {}

    override fun toMeta(): Meta = buildMeta {
        "type" to type
        "properties" to _config
        updateMeta()
    }
}

open class VisualGroup<T : VisualObject>(parent: VisualObject?) : AbstractVisualObject(parent), Iterable<T>, Provider {

    protected val namedChildren = HashMap<Name, T>()
    protected val unnamedChildren = ArrayList<T>()

    override val defaultTarget: String get() = VisualObject.TYPE

    override fun iterator(): Iterator<T> = (namedChildren.values + unnamedChildren).iterator()

    override fun provideTop(target: String): Map<Name, Any> {
        return when (target) {
            TYPE -> namedChildren
            else -> emptyMap()
        }
    }

    private data class Listener<T : VisualObject>(val owner: Any?, val callback: (Name?, T?) -> Unit)

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
                    namedChildren[name] = child
                }
                listeners.forEach { it.callback(name, child) }
            }
            child != null -> unnamedChildren.add(child)
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
    operator fun get(key: String): T? = namedChildren[key]
    /**
     * Get an unnamed child
     */
    operator fun get(index: Int): T? = unnamedChildren[index]

    /**
     * Append unnamed child
     */
    fun add(child: T) {
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
        "children" to unnamedChildren.map { it.toMeta() }
        //adding named children
        namedChildren.forEach {
            "children[${it.key}" to it.value.toMeta()
        }
    }

    override fun MetaBuilder.updateMeta() {
        updateChildren()
    }
}


