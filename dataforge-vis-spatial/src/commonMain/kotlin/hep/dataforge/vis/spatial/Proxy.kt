@file:UseSerializers(Point3DSerializer::class, NameSerializer::class)

package hep.dataforge.vis.spatial

import hep.dataforge.io.ConfigSerializer
import hep.dataforge.io.NameSerializer
import hep.dataforge.meta.Config
import hep.dataforge.meta.MetaBuilder
import hep.dataforge.meta.MetaItem
import hep.dataforge.names.Name
import hep.dataforge.vis.common.AbstractVisualObject
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

/**
 * A proxy [VisualObject3D] to reuse a template object
 */
@Serializable
class Proxy(val templateName: Name) : AbstractVisualObject(), VisualObject3D {

    override var position: Point3D? = null
    override var rotation: Point3D? = null
    override var scale: Point3D? = null

    @Serializable(ConfigSerializer::class)
    override var properties: Config? = null

    /**
     * Recursively search for defined template in the parent
     */
    val template: VisualObject3D
        get() = (parent as? VisualGroup3D)?.getTemplate(templateName)
            ?: error("Template with name $templateName not found in $parent")


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
    name: String = "",
    action: Proxy.() -> Unit = {}
) = Proxy(templateName).apply(action).also { set(name, it) }