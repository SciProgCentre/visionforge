package space.kscience.visionforge

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import space.kscience.dataforge.context.Global
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.MutableMeta
import space.kscience.dataforge.meta.MutableMetaProvider
import space.kscience.dataforge.meta.descriptors.Described
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.dataforge.meta.descriptors.get
import space.kscience.dataforge.misc.Type
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.asName
import space.kscience.dataforge.names.parseAsName
import space.kscience.dataforge.names.startsWith
import space.kscience.dataforge.values.Value
import space.kscience.dataforge.values.asValue
import space.kscience.dataforge.values.boolean
import space.kscience.visionforge.Vision.Companion.TYPE
import kotlin.reflect.KProperty1

/**
 * A root type for display hierarchy
 */
@Type(TYPE)
public interface Vision : Described {

    /**
     * The parent object of this one. If null, this one is a root.
     */
    public var parent: Vision?

    /**
     * Owner [VisionManager]. Used to define coroutine scope a serialization
     */
    public val manager: VisionManager get() = parent?.manager ?: Global.visionManager

    public val children: VisionChildren

    /**
     * Own properties without inheritance or styles.
     */
    public val meta: Meta

    public fun getPropertyValue(
        name: Name,
        inherit: Boolean,
        includeStyles: Boolean,
        includeDefaults: Boolean,
    ): Value?

    /**
     * Get property with given layer flags.
     * @param inherit toggles parent node property lookup. Null means inference from descriptor.
     * @param includeStyles toggles inclusion of properties from styles.
     */
    public fun getProperty(
        name: Name,
        inherit: Boolean,
        includeStyles: Boolean,
        includeDefaults: Boolean,
    ): MutableMeta = VisionProperties(this, name, descriptor?.get(name), inherit, includeStyles)

    public fun setProperty(
        name: Name,
        node: Meta?,
    )

    public fun setPropertyValue(
        name: Name,
        value: Value?,
    )

    public val propertyChanges: SharedFlow<Name>

    /**
     * Notify all listeners that a property has been changed and should be invalidated.
     * This method does not check that the property has actually changed.
     */
    public fun invalidateProperty(propertyName: Name)

    /**
     * Update this vision using a dif represented by [VisionChange].
     */
    public fun update(change: VisionChange)

    override val descriptor: MetaDescriptor?

    public companion object {
        public const val TYPE: String = "vision"
        public val STYLE_KEY: Name = "@style".asName()
        public const val STYLE_TARGET: String = "style"

        public val VISIBLE_KEY: Name = "visible".asName()
    }
}

public fun Vision.getPropertyValue(
    name: Name,
    inherit: Boolean? = null,
    includeStyles: Boolean? = null,
    includeDefaults: Boolean = true,
    metaDescriptor: MetaDescriptor? = descriptor?.get(name),
): Value? {
    val inheritFlag = inherit ?: metaDescriptor?.inherited ?: false
    val stylesFlag = includeStyles ?: metaDescriptor?.usesStyles ?: true
    return getPropertyValue(name, inheritFlag, stylesFlag, includeDefaults)
}

public fun Vision.getPropertyValue(
    name: String,
    inherit: Boolean? = null,
    includeStyles: Boolean? = null,
    includeDefaults: Boolean = true,
    metaDescriptor: MetaDescriptor? = descriptor?.get(name),
): Value? = getPropertyValue(name.parseAsName(), inherit, includeStyles, includeDefaults, metaDescriptor)

/**
 * Compute the property based on the provided value descriptor. By default, use Vision own descriptor
 */
public fun Vision.getProperty(
    name: Name,
    inherit: Boolean? = null,
    includeStyles: Boolean? = null,
    includeDefaults: Boolean = true,
    metaDescriptor: MetaDescriptor? = descriptor?.get(name),
): MutableMeta {
    val inheritFlag = inherit ?: metaDescriptor?.inherited ?: false
    val stylesFlag = includeStyles ?: metaDescriptor?.usesStyles ?: true
    return getProperty(name, inheritFlag, stylesFlag, includeDefaults)
}


/**
 * Get [Vision] property using key as a String
 */
public fun Vision.getProperty(
    name: String,
    inherit: Boolean? = null,
    includeStyles: Boolean? = null,
    includeDefaults: Boolean = true,
    metaDescriptor: MetaDescriptor? = descriptor?.get(name),
): MutableMeta = getProperty(name.parseAsName(), inherit, includeStyles, includeDefaults, metaDescriptor)


/**
 * Vision's own non-inheritable, non-styleable properties
 */
public fun Vision.properties(
    inherit: Boolean? = null,
    useStyles: Boolean? = null,
): MutableMetaProvider = VisionProperties(this, Name.EMPTY, inherit = inherit, useStyles = useStyles)

public fun Vision.setPropertyValue(name: Name, value: Number?) {
    if (value == null) {
        setPropertyValue(name, null)
    } else {
        setPropertyValue(name, value.asValue())
    }
}

public fun Vision.setPropertyValue(name: String, value: Number?): Unit =
    setPropertyValue(name.parseAsName(), value)

public fun Vision.setPropertyValue(name: Name, value: Boolean?) {
    if (value == null) {
        setPropertyValue(name, null)
    } else {
        setPropertyValue(name, value.asValue())
    }
}

public fun Vision.setPropertyValue(name: String, value: Boolean?): Unit =
    setPropertyValue(name.parseAsName(), value)

public fun Vision.setPropertyValue(name: Name, value: String?) {
    if (value == null) {
        setPropertyValue(name, null)
    } else {
        setPropertyValue(name, value.asValue())
    }
}

public fun Vision.setPropertyValue(name: String, value: String?): Unit =
    setPropertyValue(name.parseAsName(), value)

/**
 * Control visibility of the element
 */
public var Vision.visible: Boolean?
    get() = getPropertyValue(Vision.VISIBLE_KEY)?.boolean
    set(value) {
        setPropertyValue(Vision.VISIBLE_KEY, value)
    }

/**
 * Subscribe on property updates. The subscription is bound to the given scope and canceled when the scope is canceled
 */
public fun Vision.onPropertyChange(callback: (Name) -> Unit): Job = propertyChanges.onEach {
    callback(it)
}.launchIn(manager.context)


public fun <V : Vision, T> V.useProperty(
    property: KProperty1<V, T>,
    callBack: V.(T) -> Unit,
): Job {
    //Pass initial value.
    callBack(property.get(this))
    return propertyChanges.onEach { name ->
        if (name.startsWith(property.name.asName())) {
            callBack(property.get(this@useProperty))
        }
    }.launchIn(manager.context)
}


public interface MutableVisionGroup : Vision {

    override val children: MutableVisionChildren

    public fun createGroup(): MutableVisionGroup
}
