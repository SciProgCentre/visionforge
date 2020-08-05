package hep.dataforge.vision

import hep.dataforge.meta.*
import hep.dataforge.meta.descriptors.NodeDescriptor
import hep.dataforge.names.Name
import hep.dataforge.names.asName
import hep.dataforge.values.Value
import hep.dataforge.values.ValueType
import hep.dataforge.vision.Vision.Companion.STYLE_KEY
import kotlinx.serialization.Transient

internal data class PropertyListener(
    val owner: Any? = null,
    val action: (name: Name, oldItem: MetaItem<*>?, newItem: MetaItem<*>?) -> Unit
)

abstract class AbstractVisualObject : Vision {

    @Transient
    override var parent: VisionGroup? = null

    /**
     * Object own properties excluding styles and inheritance
     */
    protected abstract var ownProperties: Config?

    final override var styles: List<String>
        get() = ownProperties?.get(STYLE_KEY).stringList
        set(value) {
            setItem(STYLE_KEY, Value.of(value))
            updateStyles(value)
        }

    protected fun updateStyles(names: List<String>) {
        styleCache = null
        names.mapNotNull { findStyle(it) }.asSequence()
            .flatMap { it.items.asSequence() }
            .distinctBy { it.key }
            .forEach {
                propertyChanged(it.key.asName(), null, it.value)
            }
    }

    /**
     * The config is initialized and assigned on-demand.
     * To avoid unnecessary allocations, one should access [ownProperties] via [getProperty] instead.
     */
    override val config: Config
        get() = ownProperties ?: Config().also { config ->
            ownProperties = config.apply { onChange(this, ::propertyChanged) }
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
     * Collect all styles for this object in a single cached meta
     */
    protected val mergedStyles: Meta
        get() = styleCache ?: findAllStyles().merge().also {
            styleCache = it
        }

    /**
     * All available properties in a layered form
     */
    override fun properties(): Laminate = Laminate(ownProperties, mergedStyles, parent?.properties())

    override fun getProperty(name: Name, inherit: Boolean): MetaItem<*>? {
        return if (inherit) {
            sequence {
                yield(ownProperties?.get(name))
                yield(mergedStyles[name])
                yield(parent?.getProperty(name, inherit))
            }.merge()
        } else {
            sequence {
                yield(ownProperties?.get(name))
                yield(mergedStyles[name])
            }.merge()
        }
    }

    /**
     * Reset all properties to their default values
     */
    fun resetProperties() {
        ownProperties?.removeListener(this)
        ownProperties = null
    }

    companion object {
        val descriptor = NodeDescriptor {
            value(STYLE_KEY) {
                type(ValueType.STRING)
                multiple = true
            }
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