package hep.dataforge.vis.common

import hep.dataforge.meta.Config
import hep.dataforge.meta.Configurable
import hep.dataforge.meta.MetaItem
import hep.dataforge.meta.MetaRepr
import hep.dataforge.names.Name
import hep.dataforge.names.asName
import hep.dataforge.names.toName
import hep.dataforge.provider.Type
import hep.dataforge.vis.common.VisualObject.Companion.TYPE
import kotlinx.serialization.Transient

//private fun Laminate.withTop(meta: Meta): Laminate = Laminate(listOf(meta) + layers)
//private fun Laminate.withBottom(meta: Meta): Laminate = Laminate(layers + meta)

/**
 * A root type for display hierarchy
 */
@Type(TYPE)
interface VisualObject : MetaRepr, Configurable {

    /**
     * The parent object of this one. If null, this one is a root.
     */
    @Transient
    var parent: VisualObject?

    /**
     * Direct properties access
     */
    val properties: Config?

    /**
     * Set property for this object
     */
    fun setProperty(name: Name, value: Any?)

    /**
     * Get property including or excluding parent properties
     */
    fun getProperty(name: Name, inherit: Boolean = true): MetaItem<*>?

    /**
     * Manually trigger property changed event. If [name] is empty, notify that the whole object is changed
     */
    fun propertyChanged(name: Name, before: MetaItem<*>? = null, after: MetaItem<*>? = null): Unit

    /**
     * Add listener triggering on property change
     */
    fun onPropertyChange(owner: Any?, action: (Name, before: MetaItem<*>?, after: MetaItem<*>?) -> Unit): Unit

    /**
     * Remove change listeners with given owner.
     */
    fun removeChangeListener(owner: Any?)

    var style: List<String>

    companion object {
        const val TYPE = "visual"
        val STYLE_KEY = "style".asName()

        //const val META_KEY = "@meta"
        //const val TAGS_KEY = "@tags"
    }
}

/**
 * Get [VisualObject] property using key as a String
 */
fun VisualObject.getProperty(key: String, inherit: Boolean = true): MetaItem<*>? = getProperty(key.toName(), inherit)

/**
 * Set [VisualObject] property using key as a String
 */
fun VisualObject.setProperty(key: String, value: Any?) = setProperty(key.toName(), value)

/**
 *  Apply style to [VisualObject] by adding it to the [style] list
 */
fun VisualObject.applyStyle(name: String) {
    style = style + name
}