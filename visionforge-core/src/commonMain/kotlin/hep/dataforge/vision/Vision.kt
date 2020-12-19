package hep.dataforge.vision

import hep.dataforge.meta.MetaItem
import hep.dataforge.meta.MutableItemProvider
import hep.dataforge.meta.descriptors.Described
import hep.dataforge.meta.descriptors.NodeDescriptor
import hep.dataforge.names.Name
import hep.dataforge.names.asName
import hep.dataforge.names.toName
import hep.dataforge.provider.Type
import hep.dataforge.vision.Vision.Companion.TYPE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Transient

/**
 * A root type for display hierarchy
 */
@Type(TYPE)
public interface Vision : Described {

    /**
     * The parent object of this one. If null, this one is a root.
     */
    @Transient
    public var parent: VisionGroup?

    /**
     * A coroutine scope for asynchronous calls and locks
     */
    public val scope: CoroutineScope get() = parent?.scope?: GlobalScope

    /**
     * A fast accessor method to get own property (no inheritance or styles).
     * Should be equivalent to `getProperty(name,false,false,false)`.
     */
    public fun getOwnProperty(name: Name): MetaItem<*>?

    /**
     * Get property.
     * @param inherit toggles parent node property lookup. Null means inference from descriptor. Default is false.
     * @param includeStyles toggles inclusion of. Null means inference from descriptor. Default is true.
     */
    public fun getProperty(
        name: Name,
        inherit: Boolean? = null,
        includeStyles: Boolean? = null,
        includeDefaults: Boolean = true,
    ): MetaItem<*>?


    /**
     * Set the property value
     */
    public fun setProperty(name: Name, item: MetaItem<*>?, notify: Boolean = true)

    /**
     * Flow of property invalidation events. It does not contain property values after invalidation since it is not clear
     * if it should include inherited properties etc.
     */
    public val propertyNameFlow: Flow<Name>


    /**
     * Notify all listeners that a property has been changed and should be invalidated
     */
    public fun notifyPropertyChanged(propertyName: Name): Unit

    /**
     * Update this vision using external meta. Children are not updated.
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
 * Own properties, excluding inheritance, styles and descriptor
 */
public val Vision.ownProperties: MutableItemProvider
    get() = object : MutableItemProvider {
        override fun getItem(name: Name): MetaItem<*>? = getOwnProperty(name)
        override fun setItem(name: Name, item: MetaItem<*>?): Unit = setProperty(name, item)
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
    override fun getItem(name: Name): MetaItem<*>? = getProperty(
        name,
        inherit = inherit,
        includeStyles = includeStyles,
        includeDefaults = includeDefaults
    )

    override fun setItem(name: Name, item: MetaItem<*>?): Unit = setProperty(name, item)
}

/**
 * Get [Vision] property using key as a String
 */
public fun Vision.getProperty(
    key: String,
    inherit: Boolean? = null,
    includeStyles: Boolean? = null,
    includeDefaults: Boolean = true,
): MetaItem<*>? = getProperty(key.toName(), inherit, includeStyles, includeDefaults)

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
