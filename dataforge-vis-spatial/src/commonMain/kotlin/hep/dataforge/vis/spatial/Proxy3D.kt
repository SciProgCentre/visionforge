package hep.dataforge.vis.spatial

import hep.dataforge.meta.MetaBuilder
import hep.dataforge.meta.MetaItem
import hep.dataforge.names.Name
import hep.dataforge.vis.common.AbstractVisualObject
import hep.dataforge.vis.common.VisualObject

/**
 * A proxy [VisualObject3D] to reuse a [template] object
 */
class Proxy3D(parent: VisualObject?, val template: VisualObject3D) : AbstractVisualObject(parent), VisualObject3D {
    override var position: Value3 = Value3()
    override var rotation: Value3 = Value3()
    override var scale: Value3 = Value3(1f, 1f, 1f)

    override fun getProperty(name: Name, inherit: Boolean): MetaItem<*>? {
        return if (inherit) {
            super.getProperty(name, false) ?: template.getProperty(name, false) ?: parent?.getProperty(name, inherit)
        } else {
            super.getProperty(name, false) ?: template.getProperty(name, false)
        }
    }

    override fun MetaBuilder.updateMeta() {
        updatePosition()
    }
}

inline fun VisualGroup3D.proxy(
    template: VisualObject3D,
    name: String? = null,
    action: Proxy3D.() -> Unit = {}
) = Proxy3D(this, template).apply(action).also { set(name, it) }