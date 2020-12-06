package hep.dataforge.vision.solid

import hep.dataforge.meta.*
import hep.dataforge.meta.descriptors.NodeDescriptor
import hep.dataforge.names.*
import hep.dataforge.vision.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.collections.set

public abstract class AbstractReference : SolidBase(), VisionGroup {
    public abstract val prototype: Solid

    override fun getProperty(name: Name, inherit: Boolean): MetaItem<*>? = sequence {
        yield(properties?.get(name))
        yieldAll(getStyleItems(name))
        yield(prototype.getProperty(name))
        if (inherit) {
            yield(parent?.getProperty(name, inherit))
        }
    }.merge()

    override var styles: List<String>
        get() = (properties[Vision.STYLE_KEY]?.stringList ?: emptyList()) + prototype.styles
        set(value) {
            config[Vision.STYLE_KEY] = value
        }

    override val allProperties: Laminate
        get() = Laminate(
            properties,
            allStyles,
            prototype.allProperties,
            parent?.allProperties,
        )

    override fun attachChildren() {
        //do nothing
    }

    override val descriptor: NodeDescriptor get() = prototype.descriptor
}

/**
 * A reference [Solid] to reuse a template object
 */
@Serializable
@SerialName("solid.ref")
public class SolidReference(
    public val templateName: Name,
) : AbstractReference(), Solid {

    /**
     * Recursively search for defined template in the parent
     */
    override val prototype: Solid
        get() = (parent as? SolidGroup)?.getPrototype(templateName)
            ?: error("Prototype with name $templateName not found in $parent")

    override val styleSheet: StyleSheet get() = parent?.styleSheet ?: StyleSheet(this)

    @Transient
    private val propertyCache: HashMap<Name, Config> = HashMap()


    override val children: Map<NameToken, SolidReference.ReferenceChild>
        get() = (prototype as? VisionGroup)?.children
            ?.filter { !it.key.toString().startsWith("@") }
            ?.mapValues {
                ReferenceChild(it.key.asName())
            } ?: emptyMap()

    private fun childPropertyName(childName: Name, propertyName: Name): Name {
        return NameToken(REFERENCE_CHILD_PROPERTY_PREFIX, childName.toString()) + propertyName
    }

    private fun prototypeFor(name: Name): Solid {
        return (prototype as? SolidGroup)?.get(name) as? Solid
            ?: error("Prototype with name $name not found in $this")
    }

    //override fun findAllStyles(): Laminate = Laminate((styles + prototype.styles).mapNotNull { findStyle(it) })

    /**
     * A ProxyChild is created temporarily only to interact with properties, it does not store any values
     * (properties are stored in external cache) and created and destroyed on-demand).
     */
    public inner class ReferenceChild(public val name: Name) : AbstractReference() {

        override val prototype: Solid get() = prototypeFor(name)

        override val styleSheet: StyleSheet get() = this@SolidReference.styleSheet

        override val children: Map<NameToken, Vision>
            get() = (prototype as? VisionGroup)?.children
                ?.filter { !it.key.toString().startsWith("@") }
                ?.mapValues { (key, _) ->
                    ReferenceChild(name + key.asName())
                } ?: emptyMap()

        override var properties: Config?
            get() = propertyCache[name]
            set(value) {
                if (value == null) {
                    propertyCache.remove(name)?.also {
                        //Removing listener if it is present
                        removeChangeListener(this@SolidReference)
                    }
                } else {
                    propertyCache[name] = value.also {
                        onPropertyChange(this@SolidReference) { propertyName ->
                            this@SolidReference.propertyChanged(childPropertyName(name, propertyName))
                        }
                    }
                }
            }

    }

    public companion object {
        public const val REFERENCE_CHILD_PROPERTY_PREFIX: String = "@child"
    }
}

/**
 * Get a vision prototype if it is a [SolidReference] or vision itself if it is not
 */
public val Vision.prototype: Vision
    get() = when (this) {
        is AbstractReference -> prototype
        else -> this
    }

/**
 * Create ref for existing prototype
 */
public fun SolidGroup.ref(
    templateName: Name,
    name: String = "",
): SolidReference = SolidReference(templateName).also { set(name, it) }

/**
 * Add new [SolidReference] wrapping given object and automatically adding it to the prototypes
 */
public fun SolidGroup.ref(
    name: String,
    obj: Solid,
    templateName: Name = name.toName(),
): SolidReference {
    val existing = getPrototype(templateName)
    if (existing == null) {
        prototypes {
            this[templateName] = obj
        }
    } else if (existing != obj) {
        error("Can't add different prototype on top of existing one")
    }
    return this@ref.ref(templateName, name)
}
