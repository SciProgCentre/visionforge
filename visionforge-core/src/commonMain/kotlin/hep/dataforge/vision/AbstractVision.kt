package hep.dataforge.vision

import hep.dataforge.meta.*
import hep.dataforge.meta.descriptors.NodeDescriptor
import hep.dataforge.names.Name
import hep.dataforge.names.asName
import hep.dataforge.values.ValueType
import hep.dataforge.vision.Vision.Companion.STYLE_KEY
import kotlinx.serialization.Transient

internal data class PropertyListener(
    val owner: Any? = null,
    val action: (name: Name) -> Unit
)

abstract class AbstractVision : Vision {

    @Transient
    override var parent: VisionGroup? = null

    /**
     * Object own properties excluding styles and inheritance
     */
    abstract override var properties: Config?
        protected set

    protected fun updateStyles(names: List<String>) {
        names.mapNotNull { resolveStyle(it) }.asSequence()
            .flatMap { it.items.asSequence() }
            .distinctBy { it.key }
            .forEach {
                propertyChanged(it.key.asName())
            }
    }

    /**
     * The config is initialized and assigned on-demand.
     * To avoid unnecessary allocations, one should access [getAllProperties] via [getProperty] instead.
     */
    override val config: Config
        get() = properties ?: Config().also { config ->
            properties = config.also {
                it.onChange(this) { name, _, _ -> propertyChanged(name) }
            }
        }

    @Transient
    private val listeners = HashSet<PropertyListener>()

    override fun propertyChanged(name: Name) {
        if (name == STYLE_KEY) {
            updateStyles(properties?.get(STYLE_KEY)?.stringList ?: emptyList())
        }
        for (listener in listeners) {
            listener.action(name)
        }
    }

    override fun onPropertyChange(owner: Any?, action: (Name) -> Unit) {
        listeners.add(PropertyListener(owner, action))
    }

    override fun removeChangeListener(owner: Any?) {
        listeners.removeAll { it.owner == owner }
    }

    /**
     * All available properties in a layered form
     */
    override fun getAllProperties(): Laminate = Laminate(properties, allStyles, parent?.getAllProperties())

    override fun getProperty(name: Name, inherit: Boolean): MetaItem<*>? {
        return if (inherit) {
            sequence {
                yield(properties?.get(name))
                yieldAll(getStyleItems(name))
                yield(parent?.getProperty(name, inherit))
            }.merge()
        } else {
            sequence {
                yield(properties?.get(name))
                yieldAll(getStyleItems(name))
            }.merge()
        }
    }

    /**
     * Reset all properties to their default values
     */
    public fun resetProperties() {
        properties?.removeListener(this)
        properties = null
    }

    public companion object {
        public val descriptor: NodeDescriptor = NodeDescriptor {
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