package hep.dataforge.vis.common

import hep.dataforge.meta.*
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
interface VisualObject : Configurable {

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
     * Trigger property invalidation event. If [name] is empty, notify that the whole object is changed
     */
    fun propertyChanged(name: Name, before: MetaItem<*>?, after: MetaItem<*>?): Unit

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

        //const val META_KEY = "@meta"
        //const val TAGS_KEY = "@tags"


    }
}

fun VisualObject.getProperty(key: String, inherit: Boolean = true): MetaItem<*>? = getProperty(key.toName(), inherit)
fun VisualObject.setProperty(key: String, value: Any?) = setProperty(key.toName(), value)

/**
 * Add style name to the list of styles to be resolved later. The style with given name does not necessary exist at the moment.
 */
fun VisualObject.useStyle(name: String) {
    styles = styles + name
}

//private tailrec fun VisualObject.topGroup(): VisualGroup? {
//    val parent = this.parent
//    return if (parent == null) {
//        this as? VisualGroup
//    }
//    else {
//        parent.topGroup()
//    }
//}
//
///**
// * Add or update given style on a top-most reachable parent group and apply it to this object
// */
//fun VisualObject.useStyle(name: String, builder: MetaBuilder.() -> Unit) {
//    val styleName = name.toName()
//    topGroup()?.updateStyle(styleName, builder) ?: error("Can't find parent group for $this")
//    useStyle(styleName)
//}

tailrec fun VisualObject.findStyle(name: String): Meta? =
    (this as? VisualGroup)?.styleSheet?.get(name) ?: parent?.findStyle(name)

fun VisualObject.findAllStyles(): Laminate = Laminate(styles.mapNotNull(::findStyle))

