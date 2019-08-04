package hep.dataforge.vis.common

import hep.dataforge.io.ConfigSerializer
import hep.dataforge.meta.*
import hep.dataforge.names.Name
import hep.dataforge.provider.Type
import hep.dataforge.vis.common.VisualObject.Companion.TYPE
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

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
    @Transient
    var parent: VisualObject?

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

abstract class AbstractVisualObject : VisualObject {
    override var parent: VisualObject? = null

    @Transient
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

    @Serializable(ConfigSerializer::class)
    @SerialName("properties")
    private var _config: Config? = null
    override val config: Config
        get() = _config ?: Config().also { config ->
            _config = config
            config.onChange(this, ::propertyChanged)
        }

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
        "properties" to config
        updateMeta()
    }
}


