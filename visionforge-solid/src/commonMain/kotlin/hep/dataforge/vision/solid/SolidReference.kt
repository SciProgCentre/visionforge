package hep.dataforge.vision.solid

import hep.dataforge.meta.*
import hep.dataforge.meta.descriptors.NodeDescriptor
import hep.dataforge.names.*
import hep.dataforge.vision.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

public interface SolidReference : Vision {
    public val prototype: Solid
}

/**
 * A reference [Solid] to reuse a template object
 */
@Serializable
@SerialName("solid.ref")
public class SolidReferenceGroup(
    public val templateName: Name,
) : SolidBase(), SolidReference, VisionGroup {

    /**
     * Recursively search for defined template in the parent
     */
    override val prototype: Solid
        get() = (parent as? SolidGroup)?.getPrototype(templateName)
            ?: error("Prototype with name $templateName not found in $parent")

    override val children: Map<NameToken, Vision>
        get() = (prototype as? VisionGroup)?.children
            ?.filter { !it.key.toString().startsWith("@") }
            ?.mapValues {
                ReferenceChild(it.key.asName())
            } ?: emptyMap()

    private fun childToken(childName: Name): NameToken =
        NameToken(REFERENCE_CHILD_PROPERTY_PREFIX, childName.toString())

    private fun childPropertyName(childName: Name, propertyName: Name): Name =
        childToken(childName) + propertyName

    private fun getChildProperty(childName: Name, propertyName: Name): MetaItem<*>? {
        return getOwnProperty(childPropertyName(childName, propertyName))
    }

    private fun setChildProperty(childName: Name, propertyName: Name, item: MetaItem<*>?, notify: Boolean) {
        setProperty(childPropertyName(childName, propertyName), item, notify)
    }

    private fun prototypeFor(name: Name): Solid {
        return if (name.isEmpty()) prototype else {
            (prototype as? SolidGroup)?.get(name) as? Solid
                ?: error("Prototype with name $name not found in $this")
        }
    }

    override fun getProperty(
        name: Name,
        inherit: Boolean,
        includeStyles: Boolean,
        includeDefaults: Boolean,
    ): MetaItem<*>? = sequence {
        yield(getOwnProperty(name))
        if (includeStyles) {
            yieldAll(getStyleItems(name))
        }
        yield(prototype.getProperty(name, inherit, includeStyles, includeDefaults))
        if (inherit) {
            yield(parent?.getProperty(name, inherit))
        }
    }.merge()

    override fun attachChildren() {
        //do nothing
    }

    override val descriptor: NodeDescriptor get() = prototype.descriptor


    /**
     * A ProxyChild is created temporarily only to interact with properties, it does not store any values
     * (properties are stored in external cache) and created and destroyed on-demand).
     */
    private inner class ReferenceChild(private val childName: Name) : SolidReference, VisionGroup {

        override val prototype: Solid get() = prototypeFor(childName)

        override val children: Map<NameToken, Vision>
            get() = (prototype as? VisionGroup)?.children
                ?.filter { !it.key.toString().startsWith("@") }
                ?.mapValues { (key, _) ->
                    ReferenceChild(childName + key.asName())
                } ?: emptyMap()

        override fun getOwnProperty(name: Name): MetaItem<*>? = getChildProperty(childName, name)

        override fun setProperty(name: Name, item: MetaItem<*>?, notify: Boolean) {
            setChildProperty(childName, name, item, notify)
        }

        override fun getProperty(
            name: Name,
            inherit: Boolean,
            includeStyles: Boolean,
            includeDefaults: Boolean,
        ): MetaItem<*>? = sequence {
            yield(getOwnProperty(name))
            if (includeStyles) {
                yieldAll(getStyleItems(name))
            }
            yield(prototype.getProperty(name, inherit, includeStyles, includeDefaults))
            if (inherit) {
                yield(parent?.getProperty(name, inherit))
            }
        }.merge()

        override var parent: VisionGroup?
            get() {
                val parentName = childName.cutLast()
                return if (parentName.isEmpty()) this@SolidReferenceGroup else ReferenceChild(parentName)
            }
            set(value) {
                error("Setting a parent for a reference child is not possible")
            }

        override val propertyNameFlow: Flow<Name>
            get() = this@SolidReferenceGroup.propertyNameFlow.filter { name ->
                name.startsWith(childToken(childName))
            }.map { name ->
                name.cutFirst()
            }

        override fun notifyPropertyChanged(propertyName: Name) {
            this@SolidReferenceGroup.notifyPropertyChanged(childPropertyName(childName, propertyName))
        }

        override fun update(change: VisionChange) {
            TODO("Not yet implemented")
        }

        override fun attachChildren() {
            //do nothing
        }

        override val descriptor: NodeDescriptor get() = prototype.descriptor

    }

    public companion object {
        public const val REFERENCE_CHILD_PROPERTY_PREFIX: String = "@child"
    }
}

/**
 * Get a vision prototype if it is a [SolidReferenceGroup] or vision itself if it is not
 */
public val Vision.prototype: Vision
    get() = if (this is SolidReference) prototype else this

/**
 * Create ref for existing prototype
 */
public fun SolidGroup.ref(
    templateName: Name,
    name: String = "",
): SolidReferenceGroup = SolidReferenceGroup(templateName).also { set(name, it) }

/**
 * Add new [SolidReferenceGroup] wrapping given object and automatically adding it to the prototypes
 */
public fun SolidGroup.ref(
    name: String,
    obj: Solid,
    templateName: Name = name.toName(),
): SolidReferenceGroup {
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
