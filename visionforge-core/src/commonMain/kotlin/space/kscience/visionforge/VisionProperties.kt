package space.kscience.visionforge

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Transient
import space.kscience.dataforge.meta.*
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.dataforge.meta.descriptors.get
import space.kscience.dataforge.names.*

public interface VisionProperties : MetaProvider {

    /**
     * Raw Visions own properties without styles, defaults, etc.
     */
    public val own: Meta

    public val descriptor: MetaDescriptor?

    public fun getValue(
        name: Name,
        inherit: Boolean?,
        includeStyles: Boolean? = null,
    ): Value?

    override fun getValue(name: Name): Value? = getValue(name, null, null)

    /**
     * Get property with given layer flags.
     * @param inherit toggles parent node property lookup. Null means inference from descriptor.
     * @param includeStyles toggles inclusion of properties from styles.
     */
    public fun get(
        name: Name,
        inherit: Boolean?,
        includeStyles: Boolean? = null,
    ): Meta

    override fun get(name: Name): Meta? = get(name, null, null)


    public val changes: Flow<Name>

    @Deprecated("Replace with property", ReplaceWith("changes"))
    public fun flowChanges(): Flow<Name> = changes


    /**
     * Notify all listeners that a property has been changed and should be invalidated.
     * This method does not check that the property has actually changed.
     */
    public fun invalidate(propertyName: Name)
}

public interface MutableVisionProperties : VisionProperties, MutableMetaProvider {

    override fun get(
        name: Name,
        inherit: Boolean?,
        includeStyles: Boolean?,
    ): MutableMeta = VisionPropertiesItem(
        this,
        name,
        inherit,
        includeStyles,
    )

    public fun set(
        name: Name,
        item: Meta?,
        notify: Boolean,
    )

    public fun setValue(
        name: Name,
        value: Value?,
        notify: Boolean,
    )

    override fun get(name: Name): MutableMeta = get(name, null, null)

    override fun set(name: Name, node: Meta?) {
        set(name, node, true)
    }

    override fun setValue(name: Name, value: Value?) {
        setValue(name, value, true)
    }
}

public fun MutableVisionProperties.remove(name: Name) {
    set(name, null)
}

public fun MutableVisionProperties.remove(name: String) {
    remove(name.parseAsName())
}

@VisionBuilder
public operator fun MutableVisionProperties.invoke(block: MutableMeta.() -> Unit) {
    root(inherit = false, includeStyles = false).apply(block)
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
            val metaKeys = properties.own[nodeName]?.items?.keys ?: emptySet()
            val descriptorKeys = descriptor?.nodes?.map { NameToken(it.key) } ?: emptySet()
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

    override fun set(name: Name, node: Meta?) {
        properties[nodeName + name] = node
    }

    override fun toString(): String = Meta.toString(this)
    override fun equals(other: Any?): Boolean = Meta.equals(this, other as? Meta)
    override fun hashCode(): Int = Meta.hashCode(this)
}

/**
 * A base implementation of [MutableVisionProperties]
 */
public open class AbstractVisionProperties(
    public val vision: Vision,
    final override val own: MutableMeta,
) : MutableVisionProperties {
    override val descriptor: MetaDescriptor? get() = vision.descriptor

    private val descriptorCache = HashMap<Name, MetaDescriptor?>()

    override fun getValue(
        name: Name,
        inherit: Boolean?,
        includeStyles: Boolean?,
    ): Value? {
        own[name]?.value?.let { return it }

        val descriptor = descriptor?.let { descriptor -> descriptorCache.getOrPut(name) { descriptor[name] } }
        val stylesFlag = includeStyles ?: descriptor?.usesStyles ?: true

        if (stylesFlag) {
            vision.getStyleProperty(name)?.value?.let { return it }
        }

        val inheritFlag = inherit ?: descriptor?.inherited ?: false
        if (inheritFlag) {
            vision.parent?.properties?.getValue(name, inheritFlag, stylesFlag)?.let { return it }
        }
        return descriptor?.defaultValue
    }

    override fun set(name: Name, item: Meta?, notify: Boolean) {
        //ignore if the value is the same as existing
        if (own[name] == item) return

        if (name.isEmpty()) {
            if (item == null) {
                own.items.keys.forEach {
                    remove(it.asName())
                }
            } else {
                (own.items.keys - item.items.keys).forEach {
                    remove(it.asName())
                }
                item.items.forEach { (token, item) ->
                    set(token, item)
                }
            }

        } else if (item == null) {
            own[name] = item
        } else {
            own[name] = item
        }
        if (notify) {
            invalidate(name)
        }
    }

    override fun setValue(name: Name, value: Value?, notify: Boolean) {
        //ignore if the value is the same as existing
        if (own.getValue(name) == value) return

        if (value == null) {
            own[name]?.value = null
        } else {
            own.setValue(name, value)
        }
        if (notify) {
            invalidate(name)
        }
    }

    @Transient
    protected val changesInternal: MutableSharedFlow<Name> = MutableSharedFlow()

    override val changes: Flow<Name>
        get() = changesInternal

    override fun invalidate(propertyName: Name) {
        //send update signal
        val manager = vision.manager
        if (manager != null) {
            manager.context.launch {
                changesInternal.emit(propertyName)
            }
        } else {
            changesInternal.tryEmit(propertyName)
        }

        //notify children if there are any
        if (vision is VisionGroup) {
            vision.children.values.forEach {
                it.properties.invalidate(propertyName)
            }
        }

        // update styles
        if (propertyName == Vision.STYLE_KEY) {
            vision.styles.asSequence()
                .mapNotNull { vision.getStyle(it) }
                .flatMap { it.items.asSequence() }
                .distinctBy { it.key }
                .forEach {
                    invalidate(it.key.asName())
                }
        }
    }
}

public fun VisionProperties.getValue(
    name: String,
    inherit: Boolean,
    includeStyles: Boolean? = null,
): Value? = getValue(name.parseAsName(), inherit, includeStyles)

/**
 * Get [Vision] property using key as a String
 */
public operator fun VisionProperties.get(
    name: String,
    inherit: Boolean? = null,
    includeStyles: Boolean? = null,
): Meta = get(name.parseAsName(), inherit, includeStyles)

/**
 * The root property node with given inheritance and style flags
 * @param inherit inherit properties from the [Vision] parent. If null, infer from descriptor
 * @param includeStyles include style information. If null, infer from descriptor
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
