@file:UseSerializers(Point3DSerializer::class)

package hep.dataforge.vis.spatial

import hep.dataforge.meta.Config
import hep.dataforge.meta.Laminate
import hep.dataforge.meta.MetaItem
import hep.dataforge.meta.get
import hep.dataforge.names.*
import hep.dataforge.vis.*
import kotlinx.serialization.SerialName
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
@SerialName("3d.proxy")
class Proxy private constructor(
    val templateName: Name
) : AbstractVisualObject(), VisualGroup, VisualObject3D {

    constructor(parent: VisualGroup3D, templateName: Name) : this(templateName) {
        this.parent = parent
    }

    override var position: Point3D? = null
    override var rotation: Point3D? = null
    override var scale: Point3D? = null

    override var properties: Config? = null

    /**
     * Recursively search for defined template in the parent
     */
    val prototype: VisualObject3D
        get() = (parent as? VisualGroup3D)?.getPrototype(templateName)
            ?: error("Prototype with name $templateName not found in $parent")

    override val styleSheet: StyleSheet
        get() = parent?.styleSheet ?: StyleSheet(
            this
        )

    override fun getProperty(name: Name, inherit: Boolean): MetaItem<*>? {
        return if (inherit) {
            properties?.get(name)
                ?: mergedStyles[name]
                ?: prototype.getProperty(name)
                ?: parent?.getProperty(name)
        } else {
            properties?.get(name)
                ?: mergedStyles[name]
                ?: prototype.getProperty(name, false)
        }
    }

    override val children: Map<NameToken, ProxyChild>
        get() = (prototype as? VisualGroup)?.children
            ?.filter { !it.key.toString().startsWith("@") }
            ?.mapValues {
                ProxyChild(it.key.asName())
            } ?: emptyMap()

    @Transient
    private val propertyCache: HashMap<Name, Config> = HashMap()

    fun childPropertyName(childName: Name, propertyName: Name): Name {
        return NameToken(PROXY_CHILD_PROPERTY_PREFIX, childName.toString()) + propertyName
    }

    private fun prototypeFor(name: Name): VisualObject {
        return (prototype as? VisualGroup)?.get(name)
            ?: error("Prototype with name $name not found in $this")
    }

    override fun allProperties(): Laminate = Laminate(properties, mergedStyles, prototype.allProperties())

    override fun attachChildren() {
        //do nothing
    }

    //override fun findAllStyles(): Laminate = Laminate((styles + prototype.styles).mapNotNull { findStyle(it) })

    inner class ProxyChild(val name: Name) : AbstractVisualObject(),
        VisualGroup {

        val prototype: VisualObject get() = prototypeFor(name)

        override val styleSheet: StyleSheet get() = this@Proxy.styleSheet

        override val children: Map<NameToken, VisualObject>
            get() = (prototype as? VisualGroup)?.children?.mapValues { (key, _) ->
                ProxyChild(
                    name + key.asName()
                )
            } ?: emptyMap()

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
                    ?: prototype.getProperty(name)
                    ?: parent?.getProperty(name)
            } else {
                properties?.get(name)
                    ?: mergedStyles[name]
                    ?: prototype.getProperty(name, false)
            }
        }

        override fun attachChildren() {
            //do nothing
        }

        override fun allProperties(): Laminate = Laminate(properties, mergedStyles, prototype.allProperties())

    }

    companion object {
        const val PROXY_CHILD_PROPERTY_PREFIX = "@child"
    }
}

val VisualObject.prototype: VisualObject
    get() = when (this) {
        is Proxy -> prototype
        is Proxy.ProxyChild -> prototype
        else -> this
    }

/**
 * Create ref for existing prototype
 */
inline fun VisualGroup3D.ref(
    templateName: Name,
    name: String = "",
    block: Proxy.() -> Unit = {}
) = Proxy(this, templateName).apply(block).also { set(name, it) }

/**
 * Add new proxy wrapping given object and automatically adding it to the prototypes
 */
fun VisualGroup3D.proxy(
    name: String,
    obj: VisualObject3D,
    templateName: Name = name.toName(),
    block: Proxy.() -> Unit = {}
): Proxy {
    val existing = getPrototype(templateName)
    if (existing == null) {
        prototypes {
            this[templateName] = obj
        }
    } else if (existing != obj) {
        error("Can't add different prototype on top of existing one")
    }
    return ref(templateName, name, block)
}
