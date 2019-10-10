package hep.dataforge.vis.common

import hep.dataforge.meta.*
import hep.dataforge.names.EmptyName
import hep.dataforge.names.Name
import hep.dataforge.names.toName
import hep.dataforge.vis.common.VisualObject.Companion.STYLE_KEY
import kotlinx.serialization.Transient

internal data class PropertyListener(
    val owner: Any? = null,
    val action: (name: Name, oldItem: MetaItem<*>?, newItem: MetaItem<*>?) -> Unit
)

abstract class AbstractVisualObject : VisualObject {

    @Transient
    override var parent: VisualObject? = null

    abstract override var properties: Config?

    override var styles: List<Name>
        get() = properties?.get(STYLE_KEY).stringList.map(String::toName)
        set(value) {
            setProperty(STYLE_KEY, value.map { it.toString() })
            styleChanged()
        }

    /**
     * The config is initialized and assigned on-demand.
     * To avoid unnecessary allocations, one should access [properties] via [getProperty] instead.
     */
    override val config: Config
        get() = properties ?: Config().also { config ->
            properties = config.apply { onChange(this, ::propertyChanged) }
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

    override fun setProperty(name: Name, value: Any?) {
        config[name] = value
    }

    private var styleCache: Meta? = null

    /**
     * Collect all styles for this object in a laminate
     */
    protected val mergedStyles: Meta
        get() = styleCache ?: findAllStyles().merge().also {
            styleCache = it
        }


    /**
     * Helper to reset style cache
     */
    protected fun styleChanged() {
        styleCache = null
        propertyChanged(EmptyName)
    }

    override fun getProperty(name: Name, inherit: Boolean): MetaItem<*>? {
        return if (inherit) {
            properties?.get(name) ?: mergedStyles[name] ?: parent?.getProperty(name, inherit)
        } else {
            properties?.get(name) ?: mergedStyles[name]
        }
    }

    protected open fun MetaBuilder.updateMeta() {}

    override fun toMeta(): Meta = buildMeta {
        "type" to this::class.simpleName
        "properties" to properties
        updateMeta()
    }
}

fun VisualObject.findStyle(styleName: Name): Meta? {
    if (this is VisualGroup) {
        val style = getStyle(styleName)
        if (style != null) return style
    }
    return parent?.findStyle(styleName)
}