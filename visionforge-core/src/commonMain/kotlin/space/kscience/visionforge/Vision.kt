package space.kscience.visionforge

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.MutableMeta
import space.kscience.dataforge.meta.descriptors.Described
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.dataforge.meta.update
import space.kscience.dataforge.misc.Type
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.asName
import space.kscience.dataforge.values.Value
import space.kscience.visionforge.Vision.Companion.TYPE
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * A root type for display hierarchy
 */
@Type(TYPE)
public interface Vision : Described, CoroutineScope {

    /**
     * The parent object of this one. If null, this one is a root.
     */
    public var parent: VisionGroup?

    /**
     * Owner [VisionManager]. Used to define coroutine scope a serialization
     */
    public val manager: VisionManager? get() = parent?.manager

    override val coroutineContext: CoroutineContext
        get() = manager?.context?.coroutineContext ?: EmptyCoroutineContext

    /**
     * Get property.
     * @param inherit toggles parent node property lookup. Null means inference from descriptor. Default is false.
     * @param includeStyles toggles inclusion of. Null means inference from descriptor. Default is true.
     */
    public fun getProperty(
        name: Name,
        inherit: Boolean = false,
        includeStyles: Boolean = true,
        includeDefaults: Boolean = true,
    ): Meta?

    /**
     * Get an intrinsic property of this Vision excluding any inheritance or defaults. In most cases should be the same as
     * `getProperty(name, false, false, false`.
     */
    public fun getOwnProperty(name: Name): Meta? = getProperty(
        name,
        inherit = false,
        includeStyles = false,
        includeDefaults = false
    )

    /**
     * Replace the property node. If [node] is null remove node and its descendants
     */
    public fun setPropertyNode(name: Name, node: Meta?, notify: Boolean = true)

    /**
     * Set a value of specific property node
     */
    public fun setPropertyValue(name: Name, value: Value?, notify: Boolean = true)

    /**
     * Flow of property invalidation events. It does not contain property values after invalidation since it is not clear
     * if it should include inherited properties etc.
     */
    public val propertyChanges: Flow<Name>

    /**
     * Notify all listeners that a property has been changed and should be invalidated
     */
    public fun invalidateProperty(propertyName: Name)

    /**
     * Update this vision using a dif represented by [VisionChange].
     */
    public fun change(change: VisionChange)

    override val descriptor: MetaDescriptor?

    public companion object {
        public const val TYPE: String = "vision"
        public val STYLE_KEY: Name = "@style".asName()

        public val VISIBLE_KEY: Name = "visible".asName()
    }
}

/**
 * Subscribe on property updates. The subscription is bound to the given [scope] and canceled when the scope is canceled
 */
public fun Vision.onPropertyChange(scope: CoroutineScope, callback: suspend (Name) -> Unit) {
    propertyChanges.onEach(callback).launchIn(scope)
}


/**
 * Own properties, excluding inheritance, styles and descriptor
 */
public fun Vision.meta(
    inherit: Boolean = false,
    includeStyles: Boolean = true,
    includeDefaults: Boolean = true,
): MutableMeta = VisionProperties(this, Name.EMPTY, inherit, includeStyles, includeDefaults)

public fun Vision.configure(target: Name = Name.EMPTY, block: MutableMeta.() -> Unit): Unit {
    VisionProperties(this, target).apply(block)
}

public fun Vision.configure(meta: Meta) {
    configure(Name.EMPTY) {
        update(meta)
    }
}

public fun Vision.configure(block: MutableMeta.() -> Unit): Unit = configure(Meta(block))

public fun Vision.getOwnProperty(key: String): Meta? = getOwnProperty(Name.parse(key))

/**
 * Get [Vision] property using key as a String
 */
public fun Vision.getProperty(
    key: String,
    inherit: Boolean = false,
    includeStyles: Boolean = true,
    includeDefaults: Boolean = true,
): Meta? = getProperty(Name.parse(key), inherit, includeStyles, includeDefaults)


/**
 * A convenience method to set property node or value. If Item is null, then node is removed, not a value
 */
public fun Vision.setProperty(name: Name, item: Any?) {
    when (item) {
        null -> setPropertyNode(name, null)
        is Meta -> setPropertyNode(name, item)
        is Value -> setPropertyValue(name, item)
    }
}

public fun Vision.setPropertyNode(key: String, item: Any?) {
    setProperty(Name.parse(key), item)
}
