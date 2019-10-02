@file:UseSerializers(Point3DSerializer::class, NameSerializer::class, ConfigSerializer::class)

package hep.dataforge.vis.spatial

import hep.dataforge.io.ConfigSerializer
import hep.dataforge.io.NameSerializer
import hep.dataforge.meta.*
import hep.dataforge.names.Name
import hep.dataforge.names.NameToken
import hep.dataforge.names.asName
import hep.dataforge.names.plus
import hep.dataforge.vis.common.AbstractVisualObject
import hep.dataforge.vis.common.MutableVisualGroup
import hep.dataforge.vis.common.VisualGroup
import hep.dataforge.vis.common.VisualObject
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

/**
 * A proxy [VisualObject3D] to reuse a template object
 */
@Serializable
class Proxy(val templateName: Name) : AbstractVisualObject(), VisualGroup, VisualObject3D {

    override var position: Point3D? = null
    override var rotation: Point3D? = null
    override var scale: Point3D? = null

    @Serializable(ConfigSerializer::class)
    override var properties: Config? = null

    /**
     * Recursively search for defined template in the parent
     */
    val prototype: VisualObject3D
        get() = (parent as? VisualGroup3D)?.getTemplate(templateName)
            ?: error("Template with name $templateName not found in $parent")

    override fun getStyle(name: Name): Meta? = (parent as VisualGroup?)?.getStyle(name)

    override fun setStyle(name: Name, meta: Meta) {
        (parent as VisualGroup?)?.setStyle(name, meta)
        //do nothing
    }

    override fun getProperty(name: Name, inherit: Boolean): MetaItem<*>? {
        return if (inherit) {
            super.getProperty(name, false) ?: prototype.getProperty(name, false) ?: parent?.getProperty(name, inherit)
        } else {
            super.getProperty(name, false) ?: prototype.getProperty(name, false)
        }
    }

    override fun MetaBuilder.updateMeta() {
        //TODO add reference to child
        updatePosition()
    }

    override val children: Map<NameToken, ProxyChild>
        get() = (prototype as? MutableVisualGroup)?.children?.mapValues {
            ProxyChild(it.key.asName())
        } ?: emptyMap()

    private val propertyCache: HashMap<Name, Config> = HashMap()

    inner class ProxyChild(val name: Name) : AbstractVisualObject(), VisualGroup {

        override val children: Map<NameToken, VisualObject>
            get() = ((prototype as? MutableVisualGroup)?.get(name) as? MutableVisualGroup)
                ?.children
                ?.mapValues { (key, _) ->
                    ProxyChild(
                        name + key.asName()
                    )
                }
                ?: emptyMap()

        override fun getStyle(name: Name): Meta? = this@Proxy.getStyle(name)

        override fun setStyle(name: Name, meta: Meta) {
            this@Proxy.setStyle(name, meta)
        }

        val prototype: VisualObject
            get() = (this@Proxy.prototype as? VisualGroup)?.get(name)
                ?: error("Prototype with name $name not found in ${this@Proxy}")

        override var properties: Config?
            get() = propertyCache[name]
            set(value) {
                if (value == null) {
                    propertyCache.remove(name)
                } else {
                    propertyCache[name] = value
                }
            }

        override fun getProperty(name: Name, inherit: Boolean): MetaItem<*>? {
            return if (inherit) {
                properties?.get(name)
                    ?: parent?.getProperty(name, inherit)
                    ?: actualStyles[name]
                    ?: prototype.getProperty(name, inherit)
            } else {
                properties?.get(name)
                    ?: actualStyles[name]
                    ?: prototype.getProperty(name, inherit)
            }
        }
    }
}

val VisualObject.prototype: VisualObject? get() = when(this){
    is Proxy -> prototype
    is Proxy.ProxyChild -> prototype
    else -> null
}

inline fun VisualGroup3D.ref(
    templateName: Name,
    name: String = "",
    action: Proxy.() -> Unit = {}
) = Proxy(templateName).apply(action).also { set(name, it) }