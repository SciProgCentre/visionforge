@file:UseSerializers(Point3DSerializer::class)

package hep.dataforge.vision.solid

import hep.dataforge.meta.*
import hep.dataforge.meta.descriptors.NodeDescriptor
import hep.dataforge.names.*
import hep.dataforge.values.asValue
import hep.dataforge.vision.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.UseSerializers
import kotlin.collections.set


class AbstractProxy

/**
 * A proxy [Solid] to reuse a template object
 */
@Serializable
@SerialName("solid.proxy")
class Proxy private constructor(
    val templateName: Name
) : AbstractVision(), VisionGroup, Solid {

    constructor(parent: SolidGroup, templateName: Name) : this(templateName) {
        this.parent = parent
    }

    override var position: Point3D? = null
    override var rotation: Point3D? = null
    override var scale: Point3D? = null

    override var properties: Config? = null

    /**
     * Recursively search for defined template in the parent
     */
    val prototype: Solid
        get() = (parent as? SolidGroup)?.getPrototype(templateName)
            ?: error("Prototype with name $templateName not found in $parent")

    override val styleSheet: StyleSheet get() = parent?.styleSheet ?: StyleSheet(this)

    override fun getProperty(name: Name, inherit: Boolean): MetaItem<*>? {
        return if (inherit) {
            sequence {
                yield(properties?.get(name))
                yieldAll(getStyleItems(name))
                yield(prototype.getItem(name))
                yield(parent?.getProperty(name, inherit))
            }.merge()
        } else {
            sequence {
                yield(properties?.get(name))
                yieldAll(getStyleItems(name))
                yield(prototype.getProperty(name, false))
            }.merge()
        }
    }

    override val children: Map<NameToken, ProxyChild>
        get() = (prototype as? VisionGroup)?.children
            ?.filter { !it.key.toString().startsWith("@") }
            ?.mapValues {
                ProxyChild(it.key.asName())
            } ?: emptyMap()

    @Transient
    private val propertyCache: HashMap<Name, Config> = HashMap()

    fun childPropertyName(childName: Name, propertyName: Name): Name {
        return NameToken(PROXY_CHILD_PROPERTY_PREFIX, childName.toString()) + propertyName
    }

    private fun prototypeFor(name: Name): Vision {
        return (prototype as? VisionGroup)?.get(name)
            ?: error("Prototype with name $name not found in $this")
    }

    override fun getAllProperties(): Laminate =
        Laminate(properties, allStyles, prototype.getAllProperties(), parent?.getAllProperties())

    override fun attachChildren() {
        //do nothing
    }

    //override fun findAllStyles(): Laminate = Laminate((styles + prototype.styles).mapNotNull { findStyle(it) })

    override var styles: List<String>
        get() = properties[Vision.STYLE_KEY].stringList + prototype.styles
        set(value) {
            setItem(Vision.STYLE_KEY, value.map { it.asValue() }.asValue())
        }

    override val descriptor: NodeDescriptor?
        get() = prototype.descriptor

    inner class ProxyChild(val name: Name) : AbstractVision(), VisionGroup {

        val prototype: Vision get() = prototypeFor(name)

        override val styleSheet: StyleSheet get() = this@Proxy.styleSheet

        override val children: Map<NameToken, Vision>
            get() = (prototype as? VisionGroup)?.children?.mapValues { (key, _) ->
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
                        onPropertyChange(this@Proxy) { propertyName ->
                            this@Proxy.propertyChanged(childPropertyName(name, propertyName))
                        }
                    }
                }
            }

        override fun getProperty(name: Name, inherit: Boolean): MetaItem<*>? {
            return if (inherit) {
                sequence {
                    yield(properties?.get(name))
                    yieldAll(getStyleItems(name))
                    yield(prototype.getItem(name))
                    yield(parent?.getProperty(name, inherit))
                }.merge()
            } else {
                sequence {
                    yield(properties?.get(name))
                    yieldAll(getStyleItems(name))
                    yield(prototype.getProperty(name, false))
                }.merge()
            }
        }

        override fun attachChildren() {
            //do nothing
        }

        override fun getAllProperties(): Laminate =
            Laminate(properties, allStyles, prototype.getAllProperties(), parent?.getAllProperties())


        override var styles: List<String>
            get() = properties[Vision.STYLE_KEY].stringList + prototype.styles
            set(value) {
                setItem(Vision.STYLE_KEY, value.map { it.asValue() }.asValue())
            }

        override val descriptor: NodeDescriptor?
            get() = prototype.descriptor
    }

    companion object {
        const val PROXY_CHILD_PROPERTY_PREFIX = "@child"
    }
}

val Vision.prototype: Vision
    get() = when (this) {
        is Proxy -> prototype
        is Proxy.ProxyChild -> prototype
        else -> this
    }

/**
 * Create ref for existing prototype
 */
fun SolidGroup.ref(
    templateName: Name,
    name: String = ""
): Proxy = Proxy(this, templateName).also { set(name, it) }

/**
 * Add new proxy wrapping given object and automatically adding it to the prototypes
 */
fun SolidGroup.proxy(
    name: String,
    obj: Solid,
    templateName: Name = name.toName()
): Proxy {
    val existing = getPrototype(templateName)
    if (existing == null) {
        prototypes {
            this[templateName] = obj
        }
    } else if (existing != obj) {
        error("Can't add different prototype on top of existing one")
    }
    return ref(templateName, name)
}

fun SolidGroup.proxyGroup(
    name: String,
    templateName: Name = name.toName(),
    block: MutableVisionGroup.() -> Unit
): Proxy {
    val group = SolidGroup().apply(block)
    return proxy(name, group, templateName)
}
