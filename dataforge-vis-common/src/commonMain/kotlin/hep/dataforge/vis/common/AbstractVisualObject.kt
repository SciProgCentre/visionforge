package hep.dataforge.vis.common

import hep.dataforge.meta.*
import hep.dataforge.names.Name
import kotlinx.serialization.Transient

internal data class PropertyListener(
    val owner: Any? = null,
    val action: (name: Name, oldItem: MetaItem<*>?, newItem: MetaItem<*>?) -> Unit
)

abstract class AbstractVisualObject : VisualObject {

    @Transient
    override var parent: VisualObject? = null

    @Transient
    private val listeners = HashSet<PropertyListener>()

    override fun propertyChanged(name: Name, before: MetaItem<*>?, after: MetaItem<*>?) {
        for (l in listeners) {
            l.action(name, before, after)
        }
    }

    override fun onPropertyChange(owner: Any?, action: (Name, before: MetaItem<*>?, after: MetaItem<*>?) -> Unit) {
        listeners.add(PropertyListener(owner, action))
    }

    override fun removeChangeListener(owner: Any?) {
        listeners.removeAll { it.owner == owner }
    }

    abstract var properties: Config?
    override val config: Config
        get() = properties ?: Config().also { config ->
            properties = config
            config.onChange(this, ::propertyChanged)
        }

    override fun setProperty(name: Name, value: Any?) {
        config[name] = value
    }

    override fun getProperty(name: Name, inherit: Boolean): MetaItem<*>? {
        return if (inherit) {
            properties?.get(name) ?: parent?.getProperty(name, inherit)
        } else {
            properties?.get(name)
        }
    }

    protected open fun MetaBuilder.updateMeta() {}

    override fun toMeta(): Meta = buildMeta {
        "type" to this::class.simpleName
        "properties" to properties
        updateMeta()
    }
}