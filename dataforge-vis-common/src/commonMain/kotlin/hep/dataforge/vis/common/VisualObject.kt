package hep.dataforge.vis.common

import hep.dataforge.meta.*
import hep.dataforge.names.Name
import hep.dataforge.provider.Type
import hep.dataforge.vis.common.VisualObject.Companion.TYPE
import kotlinx.serialization.Transient

private fun Laminate.withTop(meta: Meta): Laminate = Laminate(listOf(meta) + layers)
private fun Laminate.withBottom(meta: Meta): Laminate = Laminate(layers + meta)

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

    companion object {
        const val TYPE = "visual"

        //const val META_KEY = "@meta"
        //const val TAGS_KEY = "@tags"
    }
}

