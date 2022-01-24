package space.kscience.visionforge

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import space.kscience.dataforge.meta.*
import space.kscience.dataforge.meta.descriptors.Described
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.dataforge.misc.DFExperimental
import space.kscience.dataforge.misc.Type
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.asName
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
public interface Vision : Described, Configurable {

    /**
     * The parent object of this one. If null, this one is a root.
     */
    public var parent: VisionGroup?

    /**
     * Owner [VisionManager]. Used to define coroutine scope a serialization
     */
    public val manager: VisionManager? get() = parent?.manager

    /**
     * This Vision own properties (ignoring inheritance, styles and defaults)
     */
    override val meta: ObservableMutableMeta

    /**
     * Get property value with given layer flags.
     * @param inherit toggles parent node property lookup. Null means inference from descriptor. Default is false.
     * @param includeStyles toggles inclusion of properties from styles. default is true
     */
    public fun getPropertyValue(
        name: Name,
        inherit: Boolean = false,
        includeStyles: Boolean = true,
        includeDefaults: Boolean = true,
    ): Value?


    /**
     * Notify all listeners that a property has been changed and should be invalidated
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

        public val VISIBLE_KEY: Name = "visible".asName()
    }
}

/**
 * Flow of property invalidation events. It does not contain property values after invalidation since it is not clear
 * if it should include inherited properties etc.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@DFExperimental
public val Vision.propertyChanges: Flow<Name>
    get() = callbackFlow {
        meta.onChange(this) { name ->
            launch {
                send(name)
            }
        }
        awaitClose {
            meta.removeListener(this)
        }
    }

/**
 * Subscribe on property updates. The subscription is bound to the given scope and canceled when the scope is canceled
 */
public fun Vision.onPropertyChange(callback: Meta.(Name) -> Unit) {
    meta.onChange(null, callback)
}

/**
 * Get [Vision] property using key as a String
 */
public fun Vision.getPropertyValue(
    key: String,
    inherit: Boolean = false,
    includeStyles: Boolean = true,
    includeDefaults: Boolean = true,
): Value? = getPropertyValue(Name.parse(key), inherit, includeStyles, includeDefaults)

/**
 * A convenience method to set property node or value. If Item is null, then node is removed, not a value
 */
public fun Vision.setProperty(name: Name, item: Any?) {
    when (item) {
        null -> meta.remove(name)
        is Meta -> meta.setMeta(name, item)
        is Value -> meta.setValue(name, item)
        else -> meta.setValue(name, Value.of(item))
    }
}

public fun Vision.setPropertyNode(key: String, item: Any?) {
    setProperty(Name.parse(key), item)
}

/**
 * Control visibility of the element
 */
public var Vision.visible: Boolean?
    get() = getPropertyValue(Vision.VISIBLE_KEY)?.boolean
    set(value) = meta.setValue(Vision.VISIBLE_KEY, value?.asValue())


public fun <V : Vision, T> V.useProperty(
    property: KProperty1<V, T>,
    owner: Any? = null,
    callBack: V.(T) -> Unit,
) {
    //Pass initial value.
    callBack(property.get(this))
    meta.onChange(owner) { name ->
        if (name.startsWith(property.name.asName())) {
            callBack(property.get(this@useProperty))
        }
    }
}