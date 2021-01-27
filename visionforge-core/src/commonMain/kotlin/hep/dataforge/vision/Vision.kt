package hep.dataforge.vision

import hep.dataforge.meta.*
import hep.dataforge.meta.descriptors.Described
import hep.dataforge.meta.descriptors.NodeDescriptor
import hep.dataforge.meta.descriptors.get
import hep.dataforge.misc.Type
import hep.dataforge.names.Name
import hep.dataforge.names.asName
import hep.dataforge.names.toName
import hep.dataforge.vision.Vision.Companion.TYPE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * A root type for display hierarchy
 */
@Type(TYPE)
public interface Vision : Described {

    /**
     * The parent object of this one. If null, this one is a root.
     */
    public var parent: VisionGroup?

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
    ): MetaItem?

    /**
     * Get an intrinsic property of this Vision excluding any inheritance or defaults. In most cases should be the same as
     * `getProperty(name, false, false, false`.
     */
    public fun getOwnProperty(name: Name): MetaItem? = getProperty(
        name,
        inherit = false,
        includeStyles = false,
        includeDefaults = false
    )

    /**
     * Set the property value
     */
    public fun setProperty(name: Name, item: MetaItem?, notify: Boolean = true)

    /**
     * Flow of property invalidation events. It does not contain property values after invalidation since it is not clear
     * if it should include inherited properties etc.
     */
    public val propertyChanges: Flow<Name>

    /**
     * Notify all listeners that a property has been changed and should be invalidated
     */
    public fun invalidateProperty(propertyName: Name): Unit

    /**
     * Update this vision using a dif represented by [VisionChange].
     */
    public fun update(change: VisionChange)

    override val descriptor: NodeDescriptor?

    public companion object {
        public const val TYPE: String = "vision"
        public val STYLE_KEY: Name = "@style".asName()

        public val VISIBLE_KEY: Name = "visible".asName()
    }
}

/**
 * Root property node
 */
public val Vision.meta: Meta get() = ownProperties[Name.EMPTY]?.node ?: Meta.EMPTY

/**
 * Subscribe on property updates. The subscription is bound to the given [scope] and canceled when the scope is canceled
 */
public fun Vision.onPropertyChange(scope: CoroutineScope, callback: suspend (Name) -> Unit) {
    propertyChanges.onEach(callback).launchIn(scope)
}


/**
 * Own properties, excluding inheritance, styles and descriptor
 */
public val Vision.ownProperties: MutableItemProvider
    get() = object : MutableItemProvider {
        override fun getItem(name: Name): MetaItem? = getOwnProperty(name)
        override fun setItem(name: Name, item: MetaItem?): Unit = setProperty(name, item)
    }

/**
 * Convenient accessor for all properties of a vision.
 * @param inherit - inherit property value from the parent by default. If null, inheritance is inferred from descriptor
 */
public fun Vision.allProperties(
    inherit: Boolean? = null,
    includeStyles: Boolean? = null,
    includeDefaults: Boolean = true,
): MutableItemProvider = object : MutableItemProvider {
    override fun getItem(name: Name): MetaItem? = getProperty(
        name,
        inherit = inherit ?: (descriptor?.get(name)?.inherited == true),
        includeStyles = includeStyles ?: (descriptor?.get(name)?.usesStyles != false),
        includeDefaults = includeDefaults
    )

    override fun setItem(name: Name, item: MetaItem?): Unit = setProperty(name, item)
}

/**
 * Get [Vision] property using key as a String
 */
public fun Vision.getProperty(
    key: String,
    inherit: Boolean = false,
    includeStyles: Boolean = true,
    includeDefaults: Boolean = true,
): MetaItem? = getProperty(key.toName(), inherit, includeStyles, includeDefaults)

/**
 * A convenience method to pair [getProperty]
 */
public fun Vision.setProperty(key: Name, item: Any?) {
    setProperty(key, MetaItem.of(item))
}

/**
 * A convenience method to pair [getProperty]
 */
public fun Vision.setProperty(key: String, item: Any?) {
    setProperty(key.toName(), MetaItem.of(item))
}
