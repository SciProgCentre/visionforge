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
    ): MetaItem? = getRefProperty(name, inherit, includeStyles, includeDefaults)

    override val descriptor: NodeDescriptor get() = prototype.descriptor


    /**
     * A ProxyChild is created temporarily only to interact with properties, it does not store any values
     * (properties are stored in external cache) and created and destroyed on-demand).
     */
    private class ReferenceChild(
        val owner: SolidReferenceGroup,
        private val childName: Name
    ) : SolidReference, VisionGroup {

        override val prototype: Solid by lazy {
            if (childName.isEmpty()) owner.prototype else {
                val proto = (owner.prototype as? VisionGroup)?.get(childName)
                    ?: error("Prototype with name $childName not found in SolidReferenceGroup ${owner.refName}")
                proto.unref as? Solid ?: error("Prototype with name $childName is ${proto::class} but expected Solid")
            }
        }

        override val children: Map<NameToken, Vision>
            get() = (prototype as? VisionGroup)?.children
                ?.filter { it.key != SolidGroup.PROTOTYPES_TOKEN }
                ?.mapValues { (key, _) ->
                    ReferenceChild(owner, childName + key.asName())
                } ?: emptyMap()

        override fun getOwnProperty(name: Name): MetaItem? =
            owner.getOwnProperty(childPropertyName(childName, name))

        override fun setProperty(name: Name, item: MetaItem?, notify: Boolean) {
            owner.setProperty(childPropertyName(childName, name), item, notify)
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
                return if (parentName.isEmpty()) owner else ReferenceChild(owner, parentName)
            }
            set(_) {
                error("Setting a parent for a reference child is not possible")
            }

        @DFExperimental
        override val propertyChanges: Flow<Name>
            get() = owner.propertyChanges.mapNotNull { name ->
                if (name.startsWith(childToken(childName))) {
                    name.cutFirst()
                } else {
                    null
                }
            }

        override fun invalidateProperty(propertyName: Name) {
            owner.invalidateProperty(childPropertyName(childName, propertyName))
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
