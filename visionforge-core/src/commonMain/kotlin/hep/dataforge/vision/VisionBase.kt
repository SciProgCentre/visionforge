package hep.dataforge.vision

import hep.dataforge.meta.*
import hep.dataforge.meta.descriptors.NodeDescriptor
import hep.dataforge.meta.descriptors.defaultItem
import hep.dataforge.meta.descriptors.get
import hep.dataforge.names.Name
import hep.dataforge.names.asName
import hep.dataforge.names.plus
import hep.dataforge.values.Null
import hep.dataforge.values.ValueType
import hep.dataforge.vision.Vision.Companion.STYLE_KEY
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.jvm.Synchronized

internal data class PropertyListener(
    val owner: Any? = null,
    val action: (name: Name) -> Unit,
)

/**
 * A full base implementation for a [Vision]
 * @param properties Object own properties excluding styles and inheritance
 */
@Serializable
@SerialName("vision")
public open class VisionBase(internal var properties: Config? = null) : Vision {

    init {
        //used during deserialization only
        properties?.onChange(this) { name, oldItem, newItem ->
            if (oldItem != newItem) {
                scope.launch {
                    notifyPropertyChanged(name)
                }
            }
        }
    }

    @Transient
    override var parent: VisionGroup? = null

    override val meta: Meta get() = properties ?: Meta.EMPTY

    @Synchronized
    protected fun getOrCreateConfig(): Config {
        if (properties == null) {
            val newProperties = Config()
            newProperties.onChange(this) { name, oldItem, newItem ->
                if (oldItem != newItem) {
                    scope.launch {
                        notifyPropertyChanged(name)
                    }
                }
            }
            properties = newProperties
        }
        return properties!!
    }

    /**
     * A fast accessor method to get own property (no inheritance or styles
     */
    override fun getOwnProperty(name: Name): MetaItem? {
        return properties?.getItem(name)
    }

    override fun getProperty(
        name: Name,
        inherit: Boolean,
        includeStyles: Boolean,
        includeDefaults: Boolean,
    ): MetaItem? = sequence {
        yield(getOwnProperty(name))
        if (includeStyles) {
            yieldAll(getStyleItems(name))
        }
        if (inherit) {
            yield(parent?.getProperty(name, inherit, includeStyles, includeDefaults))
        }
        if (includeDefaults) {
            yield(descriptor?.get(name)?.defaultItem())
        }
    }.merge()

    override fun setProperty(name: Name, item: MetaItem?, notify: Boolean) {
        getOrCreateConfig().setItem(name, item)
        if (notify) {
            scope.launch {
                notifyPropertyChanged(name)
            }
        }
    }

    override val descriptor: NodeDescriptor? get() = null

    private suspend fun updateStyles(names: List<String>) {
        names.mapNotNull { getStyle(it) }.asSequence()
            .flatMap { it.items.asSequence() }
            .distinctBy { it.key }
            .forEach {
                notifyPropertyChanged(it.key.asName())
            }
    }


    //TODO check memory consumption for the flow
    @Transient
    private val propertyInvalidationFlow: MutableSharedFlow<Name> = MutableSharedFlow()

    @DFExperimental
    override val propertyChanges: Flow<Name> get() = propertyInvalidationFlow

    override fun onPropertyChange(scope: CoroutineScope, callback: suspend (Name) -> Unit) {
        propertyInvalidationFlow.onEach(callback).launchIn(scope)
    }

    override suspend fun notifyPropertyChanged(propertyName: Name) {
        if (propertyName == STYLE_KEY) {
            updateStyles(styles)
        }
        propertyInvalidationFlow.emit(propertyName)
    }

    override fun update(change: VisionChange) {
        change.properties?.let {
            updateProperties(Name.EMPTY, it.asMetaItem())
        }
    }

    public companion object {
        public val descriptor: NodeDescriptor = NodeDescriptor {
            value(STYLE_KEY) {
                type(ValueType.STRING)
                multiple = true
            }
        }

        public fun Vision.updateProperties(at: Name, item: MetaItem) {
            when (item) {
                is ValueItem -> {
                    if (item.value == Null) {
                        setProperty(at, null)
                    } else
                        setProperty(at, item)
                }
                is NodeItem -> item.node.items.forEach { (token, childItem) ->
                    updateProperties(at + token, childItem)
                }
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