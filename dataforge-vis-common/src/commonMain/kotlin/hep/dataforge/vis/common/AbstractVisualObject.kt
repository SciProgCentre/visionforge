package hep.dataforge.vis.common

import hep.dataforge.meta.*
import hep.dataforge.names.Name
import hep.dataforge.names.asName
import kotlinx.serialization.Transient

internal data class PropertyListener(
    val owner: Any? = null,
    val action: (name: Name, oldItem: MetaItem<*>?, newItem: MetaItem<*>?) -> Unit
)

abstract class AbstractVisualObject : VisualObject {

    @Transient
    override var parent: VisualObject? = null

    override var style: Meta? = null
        set(value) {
            //notify about style removed
            style?.items?.forEach {(name, value) ->
                propertyChanged(name.asName(), value, null)
            }
            field = value
            //notify about style adition
            value?.items?.forEach { (name, value) ->
                propertyChanged(name.asName(), null, value)
            }
        }

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

    protected abstract var properties: Config?

    override val config: Config
        get() = properties ?: Config().also { config ->
            properties = config.apply { onChange(this, ::propertyChanged) }
        }

    override fun setProperty(name: Name, value: Any?) {
        config[name] = value
    }

    override fun getProperty(name: Name, inherit: Boolean): MetaItem<*>? {
        return if (inherit) {
            style?.get(name) ?: properties?.get(name) ?: parent?.getProperty(name, inherit)
        } else {
            style?.get(name) ?: properties?.get(name)
        }
    }

    protected open fun MetaBuilder.updateMeta() {}

    override fun toMeta(): Meta = buildMeta {
        "type" to this::class.simpleName
        "properties" to properties
        updateMeta()
    }
}