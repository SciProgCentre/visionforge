package hep.dataforge.vision

import hep.dataforge.meta.Config
import hep.dataforge.meta.MetaItem
import hep.dataforge.meta.MutableMeta
import hep.dataforge.meta.descriptors.NodeDescriptor
import hep.dataforge.meta.descriptors.defaultItem
import hep.dataforge.meta.descriptors.get
import hep.dataforge.meta.update
import hep.dataforge.names.Name
import hep.dataforge.names.asName
import hep.dataforge.values.ValueType
import hep.dataforge.vision.Vision.Companion.STYLE_KEY
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
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
    public var properties: Config? = null
        private set

    @Synchronized
    private fun getOrCreateConfig(): Config {
        if (properties == null) {
            val newProperties = Config()
            properties = newProperties
            newProperties.onChange(this) { name, oldItem, newItem ->
                if (oldItem != newItem) {
                    notifyPropertyChanged(name)
                }
            }
        }
        return properties!!
    }

    /**
     * A fast accessor method to get own property (no inheritance or styles
     */
    override fun getOwnProperty(name: Name): MetaItem<*>? {
        return properties?.getItem(name)
    }

    override fun getProperty(
        name: Name,
        inherit: Boolean?,
        includeStyles: Boolean?,
        includeDefaults: Boolean,
    ): MetaItem<*>? = sequence {
        yield(getOwnProperty(name))
        if (includeStyles ?: descriptor?.get(name)?.usesStyles != false) {
            yieldAll(getStyleItems(name))
        }
        if (inherit ?: descriptor?.get(name)?.inherited == true) {
            yield(parent?.getProperty(name, inherit, includeStyles, includeDefaults))
        }
        yield(descriptor?.get(name)?.defaultItem())
    }.merge()

    @Synchronized
    override fun setProperty(name: Name, item: MetaItem<*>?, notify: Boolean) {
        getOrCreateConfig().setItem(name, item)
        if(notify) {
            notifyPropertyChanged(name)
        }
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

    override val propertyNameFlow: SharedFlow<Name> get() = _propertyInvalidationFlow

    override fun notifyPropertyChanged(propertyName: Name) {
        if (propertyName == STYLE_KEY) {
            updateStyles(styles)
        }
        scope.launch {
            _propertyInvalidationFlow.emit(propertyName)
        }
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