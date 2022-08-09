package space.kscience.visionforge

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Transient
import space.kscience.dataforge.meta.*
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.dataforge.meta.descriptors.get
import space.kscience.dataforge.names.*
import kotlin.jvm.Synchronized

public interface VisionProperties {

    /**
     * Raw Visions own properties without styles, defaults, etc.
     */
    public val raw: Meta?

    public val descriptor: MetaDescriptor?

    public fun getValue(
        name: Name,
        inherit: Boolean,
        includeStyles: Boolean,
    ): Value?

    /**
     * Get property with given layer flags.
     * @param inherit toggles parent node property lookup. Null means inference from descriptor.
     * @param includeStyles toggles inclusion of properties from styles.
     */
    public operator fun get(
        name: Name,
        inherit: Boolean,
        includeStyles: Boolean,
    ): Meta

    public val changes: Flow<Name>

    /**
     * Notify all listeners that a property has been changed and should be invalidated.
     * This method does not check that the property has actually changed.
     */
    public fun invalidate(propertyName: Name)
}

public interface MutableVisionProperties : VisionProperties {

    override operator fun get(
        name: Name,
        inherit: Boolean,
        includeStyles: Boolean,
    ): MutableMeta = VisionPropertiesItem(
        this,
        name,
        inherit,
        includeStyles,
    )


    public operator fun set(
        name: Name,
        node: Meta?,
    )

    public fun setValue(
        name: Name,
        value: Value?,
    )
}

private class VisionPropertiesItem(
    val properties: MutableVisionProperties,
    val nodeName: Name,
    val inherit: Boolean? = null,
    val useStyles: Boolean? = null,
    val default: Meta? = null,
) : MutableMeta {

    val descriptor: MetaDescriptor? by lazy { properties.descriptor?.get(nodeName) }


    override val items: Map<NameToken, MutableMeta>
        get() {
            val metaKeys = properties.raw?.getMeta(nodeName)?.items?.keys ?: emptySet()
            val descriptorKeys = descriptor?.children?.map { NameToken(it.key) } ?: emptySet()
            val defaultKeys = default?.get(nodeName)?.items?.keys ?: emptySet()
            val inheritFlag = descriptor?.inherited ?: inherit
            val stylesFlag = descriptor?.usesStyles ?: useStyles
            return (metaKeys + descriptorKeys + defaultKeys).associateWith {
                VisionPropertiesItem(
                    properties,
                    nodeName + it,
                    inheritFlag,
                    stylesFlag,
                    default
                )
            }
        }

    override var value: Value?
        get() {
            val inheritFlag = descriptor?.inherited ?: inherit ?: false
            val stylesFlag = descriptor?.usesStyles ?: useStyles ?: true
            return properties.getValue(nodeName, inheritFlag, stylesFlag) ?: default?.getValue(nodeName)
        }
        set(value) {
            properties.setValue(nodeName, value)
        }

    override fun getOrCreate(name: Name): MutableMeta = VisionPropertiesItem(
        properties,
        nodeName + name,
        inherit,
        useStyles,
        default
    )

    override fun setMeta(name: Name, node: Meta?) {
        properties[nodeName + name] = node
    }

    override fun toString(): String = Meta.toString(this)
    override fun equals(other: Any?): Boolean = Meta.equals(this, other as? Meta)
    override fun hashCode(): Int = Meta.hashCode(this)
}

public abstract class AbstractVisionProperties(
    private val vision: Vision,
) : MutableVisionProperties {
    override val descriptor: MetaDescriptor? get() = vision.descriptor
    protected val default: Meta? get() = descriptor?.defaultNode

    protected abstract var properties: MutableMeta?

    override val raw: Meta? get() = properties

    @Synchronized
    protected fun getOrCreateProperties(): MutableMeta {
        if (properties == null) {
            //TODO check performance issues
            val newProperties = MutableMeta()
            properties = newProperties
        }
        return properties!!
    }

    override fun getValue(
        name: Name,
        inherit: Boolean,
        includeStyles: Boolean,
    ): Value? {
        raw?.get(name)?.value?.let { return it }
        if (includeStyles) {
            vision.getStyleProperty(name)?.value?.let { return it }
        }
        if (inherit) {
            vision.parent?.properties?.getValue(name, inherit, includeStyles)?.let { return it }
        }
        return default?.get(name)?.value
    }

    override fun set(name: Name, node: Meta?) {
        //TODO check old value?
        if (name.isEmpty()) {
            properties = node?.asMutableMeta()
        } else if (node == null) {
            properties?.setMeta(name, node)
        } else {
            getOrCreateProperties().setMeta(name, node)
        }
        invalidate(name)
    }

    override fun setValue(name: Name, value: Value?) {
        //TODO check old value?
        if (value == null) {
            properties?.getMeta(name)?.value = null
        } else {
            getOrCreateProperties().setValue(name, value)
        }
        invalidate(name)
    }

    @Transient
    private val _changes = MutableSharedFlow<Name>(10)
    override val changes: SharedFlow<Name> get() = _changes

    override fun invalidate(propertyName: Name) {
        if (propertyName == Vision.STYLE_KEY) {
            vision.styles.asSequence()
                .mapNotNull { vision.getStyle(it) }
                .flatMap { it.items.asSequence() }
                .distinctBy { it.key }
                .forEach {
                    invalidate(it.key.asName())
                }
        }
        vision.manager.context.launch {
            _changes.emit(propertyName)
        }
    }
}

public fun VisionProperties.getValue(
    name: Name,
    inherit: Boolean? = null,
    includeStyles: Boolean? = null,
): Value? {
    val descriptor = descriptor?.get(name)
    val inheritFlag = inherit ?: descriptor?.inherited ?: false
    val stylesFlag = includeStyles ?: descriptor?.usesStyles ?: true
    return getValue(name, inheritFlag, stylesFlag)
}

public fun VisionProperties.getValue(
    name: String,
    inherit: Boolean? = null,
    includeStyles: Boolean? = null,
): Value? = getValue(name.parseAsName(), inherit, includeStyles)

/**
 * Compute the property based on the provided value descriptor. By default, use Vision own descriptor
 */
public operator fun VisionProperties.get(
    name: Name,
    inherit: Boolean? = null,
    includeStyles: Boolean? = null,
): Meta {
    val descriptor: MetaDescriptor? = descriptor?.get(name)
    val inheritFlag = inherit ?: descriptor?.inherited ?: false
    val stylesFlag = includeStyles ?: descriptor?.usesStyles ?: true
    return get(name, inheritFlag, stylesFlag)
}


/**
 * Get [Vision] property using key as a String
 */
public operator fun VisionProperties.get(
    name: String,
    inherit: Boolean? = null,
    includeStyles: Boolean? = null,
): Meta = get(name.parseAsName(), inherit, includeStyles)


/**
 * Compute the property based on the provided value descriptor. By default, use Vision own descriptor
 */
public operator fun MutableVisionProperties.get(
    name: Name,
    inherit: Boolean? = null,
    includeStyles: Boolean? = null,
): MutableMeta {
    val descriptor: MetaDescriptor? = descriptor?.get(name)
    val inheritFlag = inherit ?: descriptor?.inherited ?: false
    val stylesFlag = includeStyles ?: descriptor?.usesStyles ?: true
    return get(name, inheritFlag, stylesFlag)
}

/**
 * The root property node with given inheritance and style flags
 */
public fun MutableVisionProperties.root(
    inherit: Boolean? = null,
    includeStyles: Boolean? = null,
): MutableMeta = get(Name.EMPTY, inherit, includeStyles)


/**
 * Get [Vision] property using key as a String
 */
public operator fun MutableVisionProperties.get(
    name: String,
    inherit: Boolean? = null,
    includeStyles: Boolean? = null,
): MutableMeta = get(name.parseAsName(), inherit, includeStyles)


public operator fun MutableVisionProperties.set(name: Name, value: Number): Unit =
    setValue(name, value.asValue())

public operator fun MutableVisionProperties.set(name: String, value: Number): Unit =
    set(name.parseAsName(), value)

public operator fun MutableVisionProperties.set(name: Name, value: Boolean): Unit =
    setValue(name, value.asValue())

public operator fun MutableVisionProperties.set(name: String, value: Boolean): Unit =
    set(name.parseAsName(), value)

public operator fun MutableVisionProperties.set(name: Name, value: String): Unit =
    setValue(name, value.asValue())

public operator fun MutableVisionProperties.set(name: String, value: String): Unit =
    set(name.parseAsName(), value)

