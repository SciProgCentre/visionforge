package hep.dataforge.vision

import hep.dataforge.meta.*
import hep.dataforge.names.Name
import hep.dataforge.names.asName
import hep.dataforge.names.toName
import hep.dataforge.provider.Type
import hep.dataforge.values.asValue
import hep.dataforge.vision.Vision.Companion.TYPE
import hep.dataforge.vision.Vision.Companion.VISIBLE_KEY
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * A root type for display hierarchy
 */
@Type(TYPE)
public interface Vision : Configurable {

    /**
     * The parent object of this one. If null, this one is a root.
     */
    @Transient
    public var parent: VisionGroup?

    /**
     * Nullable version of [config] used to check if this [Vision] has custom properties
     */
    public val properties: Config?

    /**
     * All properties including styles and prototypes if present, including inherited ones
     */
    public fun getAllProperties(): Laminate

    /**
     * Get property (including styles). [inherit] toggles parent node property lookup
     */
    public fun getProperty(name: Name, inherit: Boolean): MetaItem<*>?

    /**
     * Ger a property including inherited values
     */
    override fun getItem(name: Name): MetaItem<*>? = getProperty(name, true)

    /**
     * Trigger property invalidation event. If [name] is empty, notify that the whole object is changed
     */
    public fun propertyChanged(name: Name): Unit

    /**
     * Add listener triggering on property change
     */
    public fun onPropertyChange(owner: Any?, action: (Name) -> Unit): Unit

    /**
     * Remove change listeners with given owner.
     */
    public fun removeChangeListener(owner: Any?)

    /**
     * List of names of styles applied to this object. Order matters. Not inherited.
     */
    public var styles: List<String>
        get() =  properties[STYLE_KEY]?.stringList?: emptyList()
        set(value) {
            config[STYLE_KEY] = value
        }

    /**
     * Update this vision using external meta.
     */
    public fun update(meta: Meta)

    public companion object {
        public const val TYPE: String = "vision"
        public val STYLE_KEY: Name = "@style".asName()

        public fun serializer(): PolymorphicSerializer<Vision> = PolymorphicSerializer(Vision::class)

        public val VISIBLE_KEY: Name = "visible".asName()
    }
}

/**
 * Get [Vision] property using key as a String
 */
public fun Vision.getProperty(key: String, inherit: Boolean = true): MetaItem<*>? =
    getProperty(key.toName(), inherit)

/**
 * Find a style with given name for given [Vision]. The style is not necessary applied to this [Vision].
 */
public tailrec fun Vision.resolveStyle(name: String): Meta? =
    (this as? VisionGroup)?.styleSheet?.get(name) ?: parent?.resolveStyle(name)


/**
 * Control visibility of the element
 */
public var Vision.visible: Boolean?
    get() = getItem(VISIBLE_KEY).boolean
    set(value) = setItem(VISIBLE_KEY, value?.asValue())
