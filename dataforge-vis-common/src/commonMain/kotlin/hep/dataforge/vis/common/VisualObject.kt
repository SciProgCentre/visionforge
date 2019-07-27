package hep.dataforge.vis.common

import hep.dataforge.meta.*
import hep.dataforge.names.Name
import hep.dataforge.provider.Type
import hep.dataforge.vis.common.VisualObject.Companion.META_KEY
import hep.dataforge.vis.common.VisualObject.Companion.TAGS_KEY
import hep.dataforge.vis.common.VisualObject.Companion.TYPE

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
    val parent: VisualObject?

    /**
     * Individual properties configurator
     */
    override val config: Config

    /**
     * All properties including inherited ones
     */
    val properties: Laminate

    override fun toMeta(): Meta = buildMeta {
        "type" to this::class
        "properties" to properties
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
 * A change listener for [VisualObject] configuration.
 */
fun VisualObject.onChange(owner: Any?, action: (Name, before: MetaItem<*>?, after: MetaItem<*>?) -> Unit) {
    config.onChange(owner, action)
    parent?.onChange(owner, action)
}

/**
 * Remove all meta listeners with matching owners
 */
fun VisualObject.removeChangeListener(owner: Any?) {
    config.removeListener(owner)
    parent?.removeChangeListener(owner)
}


/**
 * Additional meta not relevant to display
 */
val VisualObject.meta: Meta get() = config[META_KEY]?.node ?: EmptyMeta

val VisualObject.tags: List<String> get() = config[TAGS_KEY].stringList

/**
 * Basic [VisualObject] leaf element
 */
open class VisualLeaf(
    final override val parent: VisualObject?,
    tagRefs: Array<out Meta>
) : VisualObject {
    final override val config = Config()

    override val properties: Laminate by lazy { combineProperties(parent, config, tagRefs) }
}

internal fun combineProperties(parent: VisualObject?, config: Config, tagRefs: Array<out Meta>): Laminate {
    val list = ArrayList<Meta>(tagRefs.size + 2)
    list += config
    list.addAll(tagRefs)
    parent?.properties?.let { list.add(it) }
    return Laminate(list)
}

///**
// * A group that could contain both named and unnamed children. Unnamed children could be accessed only via
// */
//interface VisualGroup : DisplayObject, Iterable<DisplayObject>, Provider {
//    override val defaultTarget: String get() = DisplayObject.TARGET
//
//    val children
//}
