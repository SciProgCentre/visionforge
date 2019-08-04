package hep.dataforge.vis.spatial

import hep.dataforge.meta.MetaBuilder
import hep.dataforge.meta.MetaItem
import hep.dataforge.names.Name
import hep.dataforge.vis.common.AbstractVisualObject

/**
 * A proxy [VisualObject3D] to reuse a template object
 */
class Proxy(val templateName: Name) : AbstractVisualObject(), VisualObject3D {
    override var position: Value3 = Value3()
    override var rotation: Value3 = Value3()
    override var scale: Value3 = Value3(1f, 1f, 1f)

    val template by lazy { getTemplate() }

    /**
     * Recursively search for defined template in the parent
     */
    private fun getTemplate(): VisualObject3D {
        return (parent as? VisualGroup3D)?.getTemplate(templateName)
            ?: error("Template with name $templateName not found in $parent")
    }


    override fun getProperty(name: Name, inherit: Boolean): MetaItem<*>? {
        return if (inherit) {
            super.getProperty(name, false) ?: template.getProperty(name, false) ?: parent?.getProperty(name, inherit)
        } else {
            super.getProperty(name, false) ?: template.getProperty(name, false)
        }
    }

    override fun MetaBuilder.updateMeta() {
        //TODO add reference to child
        updatePosition()
    }
}

//fun VisualGroup3D.proxy(
//    templateName: Name,
//    //name: String? = null,
//    builder: VisualGroup3D.() -> Unit
//): Proxy {
//    val template = getTemplate(templateName) ?: templates.builder()
//    return Proxy(this, templateName).also { set(name, it) }
//}

inline fun VisualGroup3D.ref(
    templateName: Name,
    name: String? = null,
    action: Proxy.() -> Unit = {}
) = Proxy(templateName).apply(action).also { set(name, it) }