package hep.dataforge.vis

import hep.dataforge.meta.*
import hep.dataforge.names.Name
import hep.dataforge.names.toName
import hep.dataforge.vis.DisplayObject.Companion.META_KEY
import hep.dataforge.vis.DisplayObject.Companion.TAGS_KEY

/**
 * A root type for display hierarchy
 */
interface DisplayObject {

    /**
     * The parent object of this one. If null, this one is a root.
     */
    val parent: DisplayObject?

//    /**
//     * The type of this object. Uses `.` notation. Empty type means untyped group
//     */
//    val type: String

    val properties: MutableMeta<*>

    companion object {
        const val DEFAULT_TYPE = ""
        //const val TYPE_KEY = "@type"
        //const val CHILDREN_KEY = "@children"
        const val META_KEY = "@meta"
        const val TAGS_KEY = "@tags"
    }
}

/**
 * Get the property of this display object of parent's if not found
 */
tailrec fun DisplayObject.getProperty(name: Name): MetaItem<*>? = properties[name] ?: parent?.getProperty(name)

fun DisplayObject.getProperty(name: String): MetaItem<*>? = getProperty(name.toName())

/**
 * A change listener for [DisplayObject] configuration.
 */
fun DisplayObject.onChange(owner: Any?, action: (Name, before: MetaItem<*>?, after: MetaItem<*>?) -> Unit) {
    properties.onChange(owner, action)
    parent?.onChange(owner, action)
}

/**
 * Remove all meta listeners with matching owners
 */
fun DisplayObject.removeChangeListener(owner: Any?) {
    properties.removeListener(owner)
    parent?.removeChangeListener(owner)
}


/**
 * Additional meta not relevant to display
 */
val DisplayObject.meta: Meta get() = properties[META_KEY]?.node ?: EmptyMeta

val DisplayObject.tags: List<String> get() = properties[TAGS_KEY].stringList

///**
// * Basic [DisplayObject] leaf element
// */
//open class DisplayLeaf(
//    override val parent: DisplayObject?,
////    override val type: String,
//    meta: Meta = EmptyMeta
//) : DisplayObject {
//    final override val properties = Styled(meta)
//}

interface DisplayGroup: DisplayObject, Iterable<DisplayObject>
