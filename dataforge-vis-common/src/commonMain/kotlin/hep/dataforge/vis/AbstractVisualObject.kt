package hep.dataforge.vis

import hep.dataforge.meta.*
import hep.dataforge.meta.scheme.setProperty
import hep.dataforge.names.Name
import hep.dataforge.names.asName
import hep.dataforge.values.Value
import hep.dataforge.vis.VisualObject.Companion.STYLE_KEY
import kotlinx.serialization.Transient

internal data class PropertyListener(
    val owner: Any? = null,
    val action: (name: Name, oldItem: MetaItem<*>?, newItem: MetaItem<*>?) -> Unit
)

abstract class AbstractVisualObject : VisualObject {

    @Transient
    override var parent: VisualObject? = null

    protected abstract var properties: Config?

    override var styles: List<String>
        get() = properties?.get(STYLE_KEY).stringList
        set(value) {
            //val allStyles = (field + value).distinct()
            setProperty(STYLE_KEY, Value.of(value))
            updateStyles(value)
        }

    protected fun updateStyles(names: List<String>) {
        names.mapNotNull { findStyle(it) }.asSequence()
            .flatMap { it.items.asSequence() }
            .distinctBy { it.key }
            .forEach {
                propertyChanged(it.key.asName(), null, it.value)
            }
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
        if (before != after) {
            for (l in listeners) {
                l.action(name, before, after)
            }
        }
    }

    override fun onPropertyChange(owner: Any?, action: (Name, before: MetaItem<*>?, after: MetaItem<*>?) -> Unit) {
        listeners.add(PropertyListener(owner, action))
    }

    override fun removeChangeListener(owner: Any?) {
        listeners.removeAll { it.owner == owner }
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
     * All available properties in a layered form
     */
    override fun allProperties(): Laminate = Laminate(properties, mergedStyles)

    override fun getProperty(name: Name, inherit: Boolean): MetaItem<*>? {
        return if (inherit) {
            properties?.get(name) ?: mergedStyles[name] ?: parent?.getProperty(name, inherit)
        } else {
            properties?.get(name) ?: mergedStyles[name]
        }
    }
}

//fun VisualObject.findStyle(styleName: Name): Meta? {
//    if (this is VisualGroup) {
//        val style = resolveStyle(styleName)
//        if (style != null) return style
//    }
//    return parent?.findStyle(styleName)
//}