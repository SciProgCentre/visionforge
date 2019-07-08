package hep.dataforge.vis.common

import hep.dataforge.meta.*
import hep.dataforge.names.Name
import hep.dataforge.names.toName
import hep.dataforge.provider.Type
import hep.dataforge.vis.common.VisualObject.Companion.META_KEY
import hep.dataforge.vis.common.VisualObject.Companion.TAGS_KEY
import hep.dataforge.vis.common.VisualObject.Companion.TYPE

/**
 * A root type for display hierarchy
 */
@Type(TYPE)
interface VisualObject : MetaRepr {

    /**
     * The parent object of this one. If null, this one is a root.
     */
    val parent: VisualObject?

    val properties: Styled

    override fun toMeta(): Meta = buildMeta(properties) {
        "type" to this::class
    }

    companion object {
        const val TYPE = "visual"

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
tailrec fun VisualObject.getProperty(name: Name): MetaItem<*>? = properties[name] ?: parent?.getProperty(name)

fun VisualObject.getProperty(name: String): MetaItem<*>? = getProperty(name.toName())

/**
 * A change listener for [VisualObject] configuration.
 */
fun VisualObject.onChange(owner: Any?, action: (Name, before: MetaItem<*>?, after: MetaItem<*>?) -> Unit) {
    properties.onChange(owner, action)
    parent?.onChange(owner, action)
}

/**
 * Remove all meta listeners with matching owners
 */
fun VisualObject.removeChangeListener(owner: Any?) {
    properties.removeListener(owner)
    parent?.removeChangeListener(owner)
}


/**
 * Additional meta not relevant to display
 */
val VisualObject.meta: Meta get() = properties[META_KEY]?.node ?: EmptyMeta

val VisualObject.tags: List<String> get() = properties[TAGS_KEY].stringList

/**
 * Basic [VisualObject] leaf element
 */
open class DisplayLeaf(
    override val parent: VisualObject?,
    meta: Meta = EmptyMeta
) : VisualObject {
    final override val properties = Styled(meta)
}

///**
// * A group that could contain both named and unnamed children. Unnamed children could be accessed only via
// */
//interface VisualGroup : DisplayObject, Iterable<DisplayObject>, Provider {
//    override val defaultTarget: String get() = DisplayObject.TARGET
//
//    val children
//}
