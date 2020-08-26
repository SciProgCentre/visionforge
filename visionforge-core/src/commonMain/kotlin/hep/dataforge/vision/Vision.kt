package hep.dataforge.vision

import hep.dataforge.meta.*
import hep.dataforge.names.Name
import hep.dataforge.names.asName
import hep.dataforge.names.toName
import hep.dataforge.provider.Type
import hep.dataforge.vision.Vision.Companion.TYPE
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.Transient

/**
 * A root type for display hierarchy
 */
@Type(TYPE)
interface Vision : Configurable {

    /**
     * The parent object of this one. If null, this one is a root.
     */
    @Transient
    var parent: VisionGroup?

    /**
     * Nullable version of [config] used to check if this [Vision] has custom properties
     */
    val properties: Config?

    /**
     * All properties including styles and prototypes if present, including inherited ones
     */
    fun getAllProperties(): Laminate

    /**
     * Get property (including styles). [inherit] toggles parent node property lookup
     */
    fun getProperty(name: Name, inherit: Boolean): MetaItem<*>?

    /**
     * Ger a property including inherited values
     */
    override fun getItem(name: Name): MetaItem<*>? = getProperty(name, true)

    /**
     * Trigger property invalidation event. If [name] is empty, notify that the whole object is changed
     */
    fun propertyChanged(name: Name): Unit

    /**
     * Add listener triggering on property change
     */
    fun onPropertyChange(owner: Any?, action: (Name) -> Unit): Unit

    /**
     * Remove change listeners with given owner.
     */
    fun removeChangeListener(owner: Any?)

    /**
     * List of names of styles applied to this object. Order matters. Not inherited.
     */
    var styles: List<String>
        get() =  properties[STYLE_KEY].stringList
        set(value) {
            config[STYLE_KEY] = value
        }

    companion object {
        const val TYPE = "vision"
        val STYLE_KEY = "@style".asName()

        private val VISION_SERIALIZER = PolymorphicSerializer(Vision::class)

        fun serializer() = VISION_SERIALIZER
    }
}

/**
 * Get [Vision] property using key as a String
 */
fun Vision.getProperty(key: String, inherit: Boolean = true): MetaItem<*>? =
    getProperty(key.toName(), inherit)

/**
 * Find a style with given name for given [Vision]. The style is not necessary applied to this [Vision].
 */
tailrec fun Vision.resolveStyle(name: String): Meta? =
    (this as? VisionGroup)?.styleSheet?.get(name) ?: parent?.resolveStyle(name)

