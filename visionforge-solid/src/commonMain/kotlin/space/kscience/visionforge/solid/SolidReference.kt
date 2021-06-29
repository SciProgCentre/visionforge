package space.kscience.visionforge.solid

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.dataforge.meta.MetaItem
import space.kscience.dataforge.meta.asMetaItem
import space.kscience.dataforge.meta.descriptors.NodeDescriptor
import space.kscience.dataforge.misc.DFExperimental
import space.kscience.dataforge.names.*
import space.kscience.visionforge.*

public interface SolidReference : Solid {
    public val prototype: Solid
}

private fun SolidReference.getRefProperty(
    name: Name,
    inherit: Boolean,
    includeStyles: Boolean,
    includeDefaults: Boolean,
): MetaItem? = buildList {
    add(getOwnProperty(name))
    if (includeStyles) {
        addAll(getStyleItems(name))
    }
    add(prototype.getProperty(name, inherit, includeStyles, includeDefaults))
    if (inherit) {
        add(parent?.getProperty(name, inherit))
    }
}.merge()

/**
 * A reference [Solid] to reuse a template object
 */
@Serializable
@SerialName("solid.ref")
public class SolidReferenceGroup(
    public val refName: Name,
) : SolidBase(), SolidReference, VisionGroup {

    /**
     * Recursively search for defined template in the parent
     */
    override val prototype: Solid
        get() {
            if (parent == null) error("No parent is present for SolidReferenceGroup")
            if (parent !is SolidGroup) error("Reference parent is not a group")
            return (parent as? SolidGroup)?.getPrototype(refName)
                ?: error("Prototype with name $refName not found")
        }

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

    private fun getChildProperty(childName: Name, propertyName: Name): MetaItem? {
        return getOwnProperty(childPropertyName(childName, propertyName))
    }

    private fun setChildProperty(childName: Name, propertyName: Name, item: MetaItem?, notify: Boolean) {
        setProperty(childPropertyName(childName, propertyName), item, notify)
    }

    private fun prototypeFor(name: Name): Solid {
        return if (name.isEmpty()) prototype else {
            val proto = (prototype as? SolidGroup)?.get(name)
                ?: error("Prototype with name $name not found in SolidReferenceGroup $refName")
            proto as? Solid ?: error("Prototype with name $name is ${proto::class} but expected Solid")
        }
    }

    override fun getProperty(
        name: Name,
        inherit: Boolean,
        includeStyles: Boolean,
        includeDefaults: Boolean,
    ): MetaItem? = getRefProperty(name, inherit, includeStyles, includeDefaults)

    override val descriptor: NodeDescriptor get() = prototype.descriptor


    /**
     * A ProxyChild is created temporarily only to interact with properties, it does not store any values
     * (properties are stored in external cache) and created and destroyed on-demand).
     */
    private inner class ReferenceChild(private val childName: Name) : SolidReference, VisionGroup {

        //TODO replace by properties
        override var position: Point3D?
            get() = prototype.position
            set(_) {
                error("Can't set position of reference")
            }
        override var rotation: Point3D?
            get() = prototype.rotation
            set(_) {
                error("Can't set position of reference")
            }
        override var scale: Point3D?
            get() = prototype.scale
            set(_) {
                error("Can't set position of reference")
            }
        override val prototype: Solid get() = prototypeFor(childName)

        override val children: Map<NameToken, Vision>
            get() = (prototype as? VisionGroup)?.children
                ?.filter { !it.key.toString().startsWith("@") }
                ?.mapValues { (key, _) ->
                    ReferenceChild(childName + key.asName())
                } ?: emptyMap()

        override fun getOwnProperty(name: Name): MetaItem? = getChildProperty(childName, name)

        override fun setProperty(name: Name, item: MetaItem?, notify: Boolean) {
            setChildProperty(childName, name, item, notify)
        }

        override fun getProperty(
            name: Name,
            inherit: Boolean,
            includeStyles: Boolean,
            includeDefaults: Boolean,
        ): MetaItem? = if (!inherit && !includeStyles && !includeDefaults) {
            getOwnProperty(name)
        } else {
            getRefProperty(name, inherit, includeStyles, includeDefaults)
        }

        override var parent: VisionGroup?
            get() {
                val parentName = childName.cutLast()
                return if (parentName.isEmpty()) this@SolidReferenceGroup else ReferenceChild(parentName)
            }
            set(_) {
                error("Setting a parent for a reference child is not possible")
            }

        @DFExperimental
        override val propertyChanges: Flow<Name>
            get() = this@SolidReferenceGroup.propertyChanges.mapNotNull { name ->
                if (name.startsWith(childToken(childName))) {
                    name.cutFirst()
                } else {
                    null
                }
            }

        override fun invalidateProperty(propertyName: Name) {
            this@SolidReferenceGroup.invalidateProperty(childPropertyName(childName, propertyName))
        }

        override fun update(change: VisionChange) {
            change.properties?.let {
                updateProperties(Name.EMPTY, it.asMetaItem())
            }
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
    get() = if (this is SolidReference) prototype.prototype else this

/**
 * Create ref for existing prototype
 */
public fun SolidGroup.ref(
    templateName: Name,
    name: String? = null,
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
