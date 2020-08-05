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
    fun properties(): Laminate

    /**
     * Get property including or excluding parent properties
     */
    fun getProperty(name: Name, inherit: Boolean): MetaItem<*>?

    /**
     * Ger a property including inherited values
     */
    override fun getItem(name: Name): MetaItem<*>? = getProperty(name, true)

    /**
     * Trigger property invalidation event. If [name] is empty, notify that the whole object is changed
     */
    fun propertyChanged(name: Name, before: MetaItem<*>?, after: MetaItem<*>?): Unit

    /**
     * Send a signal that property value should be reevaluated
     */
    fun propertyInvalidated(name: Name) = propertyChanged(name, null, null)

    /**
     * Add listener triggering on property change
     */
    fun onPropertyChange(owner: Any?, action: (Name, before: MetaItem<*>?, after: MetaItem<*>?) -> Unit): Unit

    /**
     * Remove change listeners with given owner.
     */
    fun removeChangeListener(owner: Any?)

    /**
     * List of names of styles applied to this object. Order matters. Not inherited
     */
    var styles: List<String>

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
 * Add style name to the list of styles to be resolved later. The style with given name does not necessary exist at the moment.
 */
fun Vision.useStyle(name: String) {
    styles = styles + name
}

tailrec fun Vision.findStyle(name: String): Meta? =
    (this as? VisionGroup)?.styleSheet?.get(name) ?: parent?.findStyle(name)

fun Vision.findAllStyles(): Laminate = Laminate(styles.mapNotNull(::findStyle))
