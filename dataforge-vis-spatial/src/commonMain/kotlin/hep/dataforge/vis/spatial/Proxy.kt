@file:UseSerializers(Point3DSerializer::class, NameSerializer::class, ConfigSerializer::class)

package hep.dataforge.vis.spatial

import hep.dataforge.io.serialization.ConfigSerializer
import hep.dataforge.io.serialization.NameSerializer
import hep.dataforge.meta.Config
import hep.dataforge.meta.Meta
import hep.dataforge.meta.MetaItem
import hep.dataforge.meta.get
import hep.dataforge.names.Name
import hep.dataforge.names.NameToken
import hep.dataforge.names.asName
import hep.dataforge.names.plus
import hep.dataforge.vis.common.AbstractVisualObject
import hep.dataforge.vis.common.MutableVisualGroup
import hep.dataforge.vis.common.VisualGroup
import hep.dataforge.vis.common.VisualObject
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
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
    val prototype: VisualObject3D
        get() = (parent as? VisualGroup3D)?.getPrototype(templateName)
            ?: error("Template with name $templateName not found in $parent")

    override fun getStyle(name: Name): Meta? = (parent as VisualGroup?)?.getStyle(name)

    override fun addStyle(name: Name, meta: Meta, apply: Boolean) {
        (parent as VisualGroup?)?.addStyle(name, meta, apply)
        //do nothing
    }

    override fun getProperty(name: Name, inherit: Boolean): MetaItem<*>? {
        return if (inherit) {
            properties?.get(name)
                ?: mergedStyles[name]
                ?: prototype.getProperty(name, false)
                ?: parent?.getProperty(name, inherit)
        } else {
            properties?.get(name)
                ?: mergedStyles[name]
                ?: prototype.getProperty(name, false)
        }
    }

    override val children: Map<NameToken, ProxyChild>
        get() = (prototype as? MutableVisualGroup)?.children
            ?.filter { !it.key.toString().startsWith("@") }
            ?.mapValues {
                ProxyChild(it.key.asName())
            } ?: emptyMap()

    @Transient
    private val propertyCache: HashMap<Name, Config> = HashMap()

    fun childPropertyName(childName: Name, propertyName: Name): Name {
        return NameToken(PROXY_CHILD_PROPERTY_PREFIX, childName.toString()) + propertyName
    }

    private fun prototypeFor(name: Name): VisualObject =
        (prototype as? VisualGroup)?.get(name)
            ?: error("Prototype with name $name not found in ${this@Proxy}")


    override var styles: List<Name>
        get() = super.styles + prototype.styles
        set(value) {
            setProperty(VisualObject.STYLE_KEY, value.map { it.toString() })
            styleChanged()
        }

    //override fun findAllStyles(): Laminate = Laminate((styles + prototype.styles).mapNotNull { findStyle(it) })

    @Serializable
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

        override fun addStyle(name: Name, meta: Meta, apply: Boolean) {
            this@Proxy.addStyle(name, meta, apply)
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
                    ?: mergedStyles[name]
                    ?: prototype.getProperty(name, inherit)
                    ?: parent?.getProperty(name, inherit)
            } else {
                properties?.get(name)
                    ?: mergedStyles[name]
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

/**
 * Create ref for existing prototype
 */
inline fun VisualGroup3D.ref(
    templateName: Name,
    name: String = "",
    block: Proxy.() -> Unit = {}
) = Proxy(templateName).apply(block).also { set(name, it) }

/**
 * Add new proxy wrapping given object and automatically adding it to the prototypes
 */
fun VisualGroup3D.proxy(
    templateName: Name,
    obj: VisualObject3D,
    name: String = "",
    attachToParent: Boolean = false,
    block: Proxy.() -> Unit = {}
): Proxy {
    val existing = getPrototype(templateName)
    if (existing == null) {
        setPrototype(templateName,obj, attachToParent)
    } else if(existing != obj) {
        error("Can't add different prototype on top of existing one")
    }
    return ref(templateName, name, block)
}
