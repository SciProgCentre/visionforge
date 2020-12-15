package hep.dataforge.vision

import hep.dataforge.meta.*
import hep.dataforge.meta.descriptors.Described
import hep.dataforge.meta.descriptors.NodeDescriptor
import hep.dataforge.names.Name
import hep.dataforge.names.asName
import hep.dataforge.names.toName
import hep.dataforge.provider.Type
import hep.dataforge.values.asValue
import hep.dataforge.vision.Vision.Companion.TYPE
import hep.dataforge.vision.Vision.Companion.VISIBLE_KEY
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
     * A fast accessor method to get own property (no inheritance or styles
     */
    public fun getOwnProperty(name: Name): MetaItem<*>?

    /**
     * Get property.
     * @param inherit toggles parent node property lookup
     * @param includeStyles toggles inclusion of
     */
    public fun getProperty(
        name: Name,
        inherit: Boolean = true,
        includeStyles: Boolean = true,
        includeDefaults: Boolean = true,
    ): MetaItem<*>?


    /**
     * Set the property value
     */
    public fun setProperty(name: Name, item: MetaItem<*>?)

    /**
     * Flow of property invalidation events. It does not contain property values after invalidation since it is not clear
     * if it should include inherited properties etc.
     */
    public val propertyInvalidated: Flow<Name>


    /**
     * Notify all listeners that a property has been changed and should be invalidated
     */
    public fun notifyPropertyChanged(propertyName: Name): Unit

    /**
     * List of names of styles applied to this object. Order matters. Not inherited.
     */
    public var styles: List<String>
        get() = getProperty(
            STYLE_KEY,
            inherit = false,
            includeStyles = false,
            includeDefaults = true
        )?.stringList ?: emptyList()
        set(value) {
            setProperty(STYLE_KEY, value)
        }

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
 * Convenient accessor for all properties of a vision. Provided properties include styles and defaults, but do not inherit.
 */
public val Vision.properties: MutableItemProvider
    get() = object : MutableItemProvider {
        override fun getItem(name: Name): MetaItem<*>? = getProperty(name,
            inherit = false,
            includeStyles = true,
            includeDefaults = true
        )

        override fun setItem(name: Name, item: MetaItem<*>?): Unit = setProperty(name, item)
    }

/**
 * Get [Vision] property using key as a String
 */
public fun Vision.getProperty(
    key: String,
    inherit: Boolean = true,
    includeStyles: Boolean = true,
    includeDefaults: Boolean = true,
): MetaItem<*>? = getProperty(key.toName(), inherit, includeStyles, includeDefaults)

/**
 * A convenience method to pair [getProperty]
 */
public fun Vision.setProperty(key: Name, value: Any?) {
    properties[key] = value
}

/**
 * A convenience method to pair [getProperty]
 */
public fun Vision.setProperty(key: String, value: Any?) {
    properties[key] = value
}

/**
 * Control visibility of the element
 */
public var Vision.visible: Boolean?
    get() = getProperty(VISIBLE_KEY).boolean
    set(value) = setProperty(VISIBLE_KEY, value?.asValue())

public fun Vision.props(inherit: Boolean = true): MutableItemProvider = object : MutableItemProvider {
    override fun getItem(name: Name): MetaItem<*>? {
        return getProperty(name, inherit)
    }

    override fun setItem(name: Name, item: MetaItem<*>?) {
        setProperty(name, item)
    }

}