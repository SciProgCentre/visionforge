package hep.dataforge.vis.common

import hep.dataforge.meta.*
import hep.dataforge.names.Name

/**
 * Basic [VisualObject] leaf element
 */
open class VisualLeaf(meta: Meta = EmptyMeta) : AbstractVisualObject(), Configurable {

    val properties = Styled(meta)

    override val config: Config = properties.style

    override fun setProperty(name: Name, value: Any?) {
        config[name] = value
    }

    override fun getProperty(name: Name, inherit: Boolean): MetaItem<*>? {
        return if (inherit) {
            properties[name] ?: parent?.getProperty(name, inherit)
        } else {
            properties[name]
        }
    }
}