package hep.dataforge.vision

import hep.dataforge.meta.*
import hep.dataforge.meta.descriptors.NodeDescriptor
import hep.dataforge.meta.descriptors.defaultItem
import hep.dataforge.meta.descriptors.defaultMeta
import hep.dataforge.meta.descriptors.get
import hep.dataforge.names.Name
import hep.dataforge.names.asName
import hep.dataforge.values.ValueType
import hep.dataforge.vision.Vision.Companion.STYLE_KEY
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

internal data class PropertyListener(
    val owner: Any? = null,
    val action: (name: Name) -> Unit,
)

@Serializable
@SerialName("vision")
public open class VisionBase : Vision {

    @Transient
    override var parent: VisionGroup? = null

    /**
     * Object own properties excluding styles and inheritance
     */
    override var properties: Config? = null
        protected set

    override val descriptor: NodeDescriptor? get() = null

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
    override val config: Config by lazy {
        properties ?: Config().also { config ->
            properties = config.also {
                it.onChange(this) { name, _, _ -> propertyChanged(name) }
            }
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
        listeners.removeAll { owner == null || it.owner == owner }
    }

    /**
     * All available properties in a layered form
     */
    override val allProperties: Laminate
        get() = Laminate(
            properties,
            allStyles,
            parent?.allProperties,
            descriptor?.defaultMeta(),
        )

    override fun getProperty(name: Name, inherit: Boolean): MetaItem<*>? = sequence {
        yield(properties?.get(name))
        yieldAll(getStyleItems(name))
        if (inherit) {
            yield(parent?.getProperty(name, inherit))
        }
        yield(descriptor?.get(name)?.defaultItem())
    }.merge()

    /**
     * Reset all properties to their default values
     */
    public fun resetProperties() {
        properties?.removeListener(this)
        properties = null
    }

    override fun update(change: VisionChange) {
        change.propertyChange[Name.EMPTY]?.let {
            config.update(it)
        }
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