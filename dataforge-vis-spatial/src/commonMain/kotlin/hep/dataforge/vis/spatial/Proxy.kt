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
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

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
    val prototype: VisualObject3D get() = (parent as? VisualGroup3D)?.getTemplate(templateName)
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

    fun childPropertyName(childName: Name, propertyName: Name): Name {
        return NameToken(PROXY_CHILD_PROPERTY_PREFIX, childName.toString()) + propertyName
    }

    private fun prototypeFor(name: Name): VisualObject =
        (prototype as? VisualGroup)?.get(name)
            ?: error("Prototype with name $name not found in ${this@Proxy}")


    inner class ProxyChild(val name: Name) : AbstractVisualObject(), VisualGroup {

        val prototype: VisualObject by lazy {
            prototypeFor(name)
        }

        override val children: Map<NameToken, VisualObject>
            get() = (prototype as? VisualGroup)?.children?.mapValues { (key, _) ->
                ProxyChild(
                    name + key.asName()
                )
            } ?: emptyMap()

        override fun getStyle(name: Name): Meta? = this@Proxy.getStyle(name)

        override fun setStyle(name: Name, meta: Meta) {
            this@Proxy.setStyle(name, meta)
        }

        override var properties: Config?
            get() = propertyCache[name]
            set(value) {
                if (value == null) {
                    propertyCache.remove(name)?.also {
                        //Removing listener if it is present
                        removeChangeListener(this@Proxy)
                    }
                } else {
                    propertyCache[name] = value.also {
                        onPropertyChange(this@Proxy) { propertyName, before, after ->
                            this@Proxy.propertyChanged(childPropertyName(name, propertyName), before, after)
                        }
                    }
                }
            }

        override fun getProperty(name: Name, inherit: Boolean): MetaItem<*>? {
            return if (inherit) {
                properties?.get(name)
                    ?: appliedStyles[name]
                    ?: parent?.getProperty(name, inherit)
                    ?: prototype.getProperty(name, inherit)
            } else {
                properties?.get(name)
                    ?: appliedStyles[name]
                    ?: prototype.getProperty(name, inherit)
            }
        }

    }

    companion object {
        const val PROXY_CHILD_PROPERTY_PREFIX = "@child"
    }
}

val VisualObject.prototype: VisualObject?
    get() = when (this) {
        is Proxy -> prototype
        is Proxy.ProxyChild -> prototype
        else -> null
    }

inline fun VisualGroup3D.ref(
    templateName: Name,
    name: String = "",
    action: Proxy.() -> Unit = {}
) = Proxy(templateName).apply(action).also { set(name, it) }