package hep.dataforge.vision

import hep.dataforge.meta.Configurable
import hep.dataforge.meta.Laminate
import hep.dataforge.meta.Meta
import hep.dataforge.meta.MetaItem
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

    companion object {
        const val TYPE = "visual"
        val STYLE_KEY = "@style".asName()

        private val VISUAL_OBJECT_SERIALIZER = PolymorphicSerializer(Vision::class)

        fun serializer() = VISUAL_OBJECT_SERIALIZER
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

