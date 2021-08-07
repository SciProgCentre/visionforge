package space.kscience.visionforge

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import space.kscience.dataforge.meta.*
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.dataforge.meta.descriptors.defaultNode
import space.kscience.dataforge.meta.descriptors.value
import space.kscience.dataforge.misc.DFExperimental
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.asName
import space.kscience.dataforge.names.plus
import space.kscience.dataforge.values.Value
import space.kscience.dataforge.values.ValueType
import space.kscience.visionforge.Vision.Companion.STYLE_KEY
import kotlin.jvm.Synchronized

/**
 * A full base implementation for a [Vision]
 * @param properties Object own properties excluding styles and inheritance
 */
@Serializable
@SerialName("vision")
public open class VisionBase(
    @Transient override var parent: VisionGroup? = null,
    @Serializable(MutableMetaSerializer::class)
    protected var properties: MutableMeta? = null
) : Vision {

    //protected val observableProperties: ObservableMutableMeta by lazy { properties.asObservable() }

    @Synchronized
    protected fun getOrCreateProperties(): MutableMeta {
        if (properties == null) {
            val newProperties = MutableMeta()
            properties = newProperties
        }
        return properties!!
    }

    /**
     * A fast accessor method to get own property (no inheritance or styles
     */
    override fun getOwnProperty(name: Name): Meta? = properties?.getMeta(name)

    override fun getProperty(
        name: Name,
        inherit: Boolean,
        includeStyles: Boolean,
        includeDefaults: Boolean,
    ): Meta? = if (!inherit && !includeStyles && !includeDefaults) {
        getOwnProperty(name)
    } else {
        buildList {
            add(getOwnProperty(name))
            if (includeStyles) {
                addAll(getStyleItems(name))
            }
            if (inherit) {
                add(parent?.getProperty(name, inherit, includeStyles, includeDefaults))
            }
            if (includeDefaults) {
                add(descriptor?.defaultNode?.get(name))
            }
        }.merge()
    }

    override fun setPropertyNode(name: Name, node: Meta?, notify: Boolean) {
        val oldItem = properties?.get(name)
        if (oldItem != node) {
            getOrCreateProperties().setMeta(name, node)
            if (notify) {
                invalidateProperty(name)
            }
        }
    }

    override fun setPropertyValue(name: Name, value: Value?, notify: Boolean) {
        val oldItem = properties?.get(name)?.value
        if (oldItem != value) {
            getOrCreateProperties()[name] = value
            if (notify) {
                invalidateProperty(name)
            }
        }
    }

    override val descriptor: MetaDescriptor? get() = null

    private suspend fun updateStyles(names: List<String>) {
        names.mapNotNull { getStyle(it) }.asSequence()
            .flatMap { it.items.asSequence() }
            .distinctBy { it.key }
            .forEach {
                invalidateProperty(it.key.asName())
            }
    }

    //TODO check memory consumption for the flow
    @Transient
    private val propertyInvalidationFlow: MutableSharedFlow<Name> = MutableSharedFlow()

    @DFExperimental
    override val propertyChanges: Flow<Name>
        get() = propertyInvalidationFlow

    override fun invalidateProperty(propertyName: Name) {
        launch {
            if (propertyName == STYLE_KEY) {
                updateStyles(styles)
            }
            propertyInvalidationFlow.emit(propertyName)
        }
    }

    override fun change(change: VisionChange) {
        change.properties?.let {
            updateProperties(Name.EMPTY, it)
        }
    }

    public companion object {
        public val descriptor: MetaDescriptor = MetaDescriptor {
            value(STYLE_KEY, ValueType.STRING) {
                multiple = true
            }
        }

        public fun Vision.updateProperties(at: Name, item: Meta) {
            setPropertyValue(at, item.value)
            item.items.forEach { (token, item) ->
                updateProperties(at + token, item)
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