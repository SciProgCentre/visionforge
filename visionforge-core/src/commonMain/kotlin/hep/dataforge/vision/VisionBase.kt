package hep.dataforge.vision

import hep.dataforge.meta.*
import hep.dataforge.meta.descriptors.NodeDescriptor
import hep.dataforge.meta.descriptors.defaultItem
import hep.dataforge.meta.descriptors.get
import hep.dataforge.names.Name
import hep.dataforge.names.asName
import hep.dataforge.values.ValueType
import hep.dataforge.vision.Vision.Companion.STYLE_KEY
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.jvm.Synchronized

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
    @SerialName("properties")
    protected var innerProperties: Config? = null
        private set

    /**
     * All own properties as a read-only Meta
     */
    public val ownProperties: Meta get() = innerProperties ?: Meta.EMPTY

    @Synchronized
    private fun getOrCreateConfig(): Config {
        if (innerProperties == null) {
            val newProperties = Config()
            innerProperties = newProperties
            newProperties.onChange(this) { name, oldItem, newItem ->
                if (oldItem != newItem) {
                    notifyPropertyChanged(name)
                }
            }
        }
        return innerProperties!!
    }

    /**
     * A fast accessor method to get own property (no inheritance or styles
     */
    override fun getOwnProperty(name: Name): MetaItem<*>? {
        return innerProperties?.getItem(name)
    }

    override fun getProperty(
        name: Name,
        inherit: Boolean,
        includeStyles: Boolean,
        includeDefaults: Boolean,
    ): MetaItem<*>? = sequence {
        yield(getOwnProperty(name))
        if (includeStyles) {
            yieldAll(getStyleItems(name))
        }
        if (inherit) {
            yield(parent?.getProperty(name, inherit))
        }
        yield(descriptor?.get(name)?.defaultItem())
    }.merge()

    @Synchronized
    override fun setProperty(name: Name, item: MetaItem<*>?) {
        getOrCreateConfig().setItem(name, item)
        notifyPropertyChanged(name)
    }

    override val descriptor: NodeDescriptor? get() = null

    private fun updateStyles(names: List<String>) {
        names.mapNotNull { getStyle(it) }.asSequence()
            .flatMap { it.items.asSequence() }
            .distinctBy { it.key }
            .forEach {
                notifyPropertyChanged(it.key.asName())
            }
    }

    @Transient
    private val _propertyInvalidationFlow: MutableSharedFlow<Name> = MutableSharedFlow()

    override val propertyInvalidated: SharedFlow<Name> get() = _propertyInvalidationFlow

    override fun notifyPropertyChanged(propertyName: Name) {
        if (propertyName == STYLE_KEY) {
            updateStyles(properties.getItem(STYLE_KEY)?.stringList ?: emptyList())
        }

        _propertyInvalidationFlow.tryEmit(propertyName)
    }

    public fun configure(block: MutableMeta<*>.() -> Unit) {
        getOrCreateConfig().block()
    }

    override fun update(change: VisionChange) {
        change.properties?.let {
            getOrCreateConfig().update(it)
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