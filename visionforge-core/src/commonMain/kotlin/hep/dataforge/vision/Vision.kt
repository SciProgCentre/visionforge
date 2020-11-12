package hep.dataforge.vision

import hep.dataforge.meta.*
import hep.dataforge.meta.descriptors.Described
import hep.dataforge.names.Name
import hep.dataforge.names.asName
import hep.dataforge.names.toName
import hep.dataforge.provider.Type
import hep.dataforge.values.asValue
import hep.dataforge.vision.Vision.Companion.TYPE
import hep.dataforge.vision.Vision.Companion.VISIBLE_KEY
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.Transient
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * A root type for display hierarchy
 */
@Type(TYPE)
public interface Vision : Configurable, Described {

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
    public fun getProperty(name: Name, inherit: Boolean = true): MetaItem<*>?

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
        get() = properties[STYLE_KEY]?.stringList ?: emptyList()
        set(value) {
            config[STYLE_KEY] = value
        }

    /**
     * Update this vision using external meta. Children are not updated.
     */
    public fun update(change: Vision)

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
 * A convenience method to pair [getProperty]
 */
public fun Vision.setProperty(key: Name, value: Any?) {
    config[key] = value
}

/**
 * A convenience method to pair [getProperty]
 */
public fun Vision.setProperty(key: String, value: Any?) {
    config[key] = value
}

/**
 * Find a style with given name for given [Vision]. The style is not necessary applied to this [Vision].
 */
public tailrec fun Vision.resolveStyle(name: String): Meta? =
    (this as? VisionGroup)?.styleSheet?.get(name) ?: parent?.resolveStyle(name)

/**
 * Control visibility of the element
 */
public var Vision.visible: Boolean?
    get() = getProperty(VISIBLE_KEY).boolean
    set(value) = config.setValue(VISIBLE_KEY, value?.asValue())

/**
 * Convinience delegate for properties
 */
public fun Vision.property(
    default: MetaItem<*>? = null,
    key: Name? = null,
    inherit: Boolean = true,
): MutableItemDelegate =
    object : ReadWriteProperty<Any?, MetaItem<*>?> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): MetaItem<*>? {
            val name = key ?: property.name.toName()
            return getProperty(name, inherit) ?: default
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: MetaItem<*>?) {
            val name = key ?: property.name.toName()
            setProperty(name, value)
        }
    }

//TODO replace by value
fun Vision.properties(inherit: Boolean = true): MutableItemProvider = object : MutableItemProvider {
    override fun getItem(name: Name): MetaItem<*>? {
        return getProperty(name, inherit)
    }

    override fun setItem(name: Name, item: MetaItem<*>?) {
        setProperty(name, item)
    }

}