package space.kscience.visionforge.solid

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.dataforge.misc.DFExperimental
import space.kscience.dataforge.names.*
import space.kscience.dataforge.values.Value
import space.kscience.visionforge.*


public interface SolidReference : VisionGroup {
    /**
     * The prototype for this reference. Always returns a "real" prototype, not a reference
     */
    public val prototype: Solid
}


/**
 * Get a vision prototype if it is a [SolidReference] or vision itself if it is not.
 * Unref is recursive, so it always returns a non-reference.
 */
public val Vision.unref: Solid
    get() = when (this) {
        is SolidReference -> prototype.unref
        is Solid -> this
        else -> error("This Vision is neither Solid nor SolidReference")
    }


private fun SolidReference.getRefProperty(
    name: Name,
    inherit: Boolean,
    includeStyles: Boolean,
    includeDefaults: Boolean,
): Meta? = if (!inherit && !includeStyles && !includeDefaults) {
    getOwnProperty(name)
} else {
    buildList {
        add(getOwnProperty(name))
        if (includeStyles) {
            addAll(getStyleItems(name))
        }
        add(prototype.getProperty(name, inherit, includeStyles, includeDefaults))
        if (inherit) {
            add(parent?.getProperty(name, inherit))
        }
    }.merge()
}

private fun childToken(childName: Name): NameToken =
    NameToken(SolidReferenceGroup.REFERENCE_CHILD_PROPERTY_PREFIX, childName.toString())

private fun childPropertyName(childName: Name, propertyName: Name): Name =
    childToken(childName) + propertyName

/**
 * A reference [Solid] to reuse a template object
 */
@Serializable
@SerialName("solid.ref")
public class SolidReferenceGroup(
    public val refName: Name,
) : VisionBase(), SolidReference, VisionGroup, Solid {

    /**
     * Recursively search for defined template in the parent
     */
    override val prototype: Solid by lazy {
        if (parent == null) error("No parent is present for SolidReferenceGroup")
        if (parent !is PrototypeHolder) error("Parent does not hold prototypes")
        (parent as? PrototypeHolder)?.getPrototype(refName) ?: error("Prototype with name $refName not found")
    }

    override val children: Map<NameToken, Vision>
        get() = (prototype as? VisionGroup)?.children
            ?.filter { it.key != SolidGroup.PROTOTYPES_TOKEN }
            ?.mapValues {
                ReferenceChild(this, it.key.asName())
            } ?: emptyMap()

    override fun getProperty(
        name: Name,
        inherit: Boolean,
        includeStyles: Boolean,
        includeDefaults: Boolean,
    ): Meta? = getRefProperty(name, inherit, includeStyles, includeDefaults)

    override val descriptor: MetaDescriptor get() = prototype.descriptor


    /**
     * A ProxyChild is created temporarily only to interact with properties, it does not store any values
     * (properties are stored in external cache) and created and destroyed on-demand).
     */
    private class ReferenceChild(
        val owner: SolidReferenceGroup,
        private val refName: Name
    ) : SolidReference, VisionGroup, Solid {

        override val prototype: Solid by lazy {
            if (refName.isEmpty()) owner.prototype else {
                val proto = (owner.prototype as? VisionGroup)?.get(refName)
                    ?: error("Prototype with name $refName not found in SolidReferenceGroup ${owner.refName}")
                proto.unref as? Solid
                    ?: error("Prototype with name $refName is ${proto::class} but expected Solid")
            }
        }

        override val children: Map<NameToken, Vision>
            get() = (prototype as? VisionGroup)?.children
                ?.filter { it.key != SolidGroup.PROTOTYPES_TOKEN }
                ?.mapValues { (key, _) ->
                    ReferenceChild(owner, refName + key.asName())
                } ?: emptyMap()

        override fun getOwnProperty(name: Name): Meta? =
            owner.getOwnProperty(childPropertyName(refName, name))

        override fun setPropertyNode(name: Name, node: Meta?, notify: Boolean) {
            owner.setPropertyNode(childPropertyName(refName, name), node, notify)
        }

        override fun setPropertyValue(name: Name, value: Value?, notify: Boolean) {
            owner.setPropertyValue(childPropertyName(refName, name), value, notify)
        }

        override fun getProperty(
            name: Name,
            inherit: Boolean,
            includeStyles: Boolean,
            includeDefaults: Boolean,
        ): Meta? = getRefProperty(name, inherit, includeStyles, includeDefaults)

        override var parent: VisionGroup?
            get() {
                val parentName = refName.cutLast()
                return if (parentName.isEmpty()) owner else ReferenceChild(owner, parentName)
            }
            set(_) {
                error("Setting a parent for a reference child is not possible")
            }

        @DFExperimental
        override val propertyChanges: Flow<Name>
            get() = owner.propertyChanges.mapNotNull { name ->
                if (name.startsWith(childToken(refName))) {
                    name.cutFirst()
                } else {
                    null
                }
            }

        override fun invalidateProperty(propertyName: Name) {
            owner.invalidateProperty(childPropertyName(refName, propertyName))
        }

        override fun change(change: VisionChange) {
            change.properties?.let {
                updateProperties(Name.EMPTY, it)
            }
        }

        override val descriptor: MetaDescriptor get() = prototype.descriptor

    }

    public companion object {
        public const val REFERENCE_CHILD_PROPERTY_PREFIX: String = "@child"
    }
}

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
    templateName: Name = Name.parse(name),
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
