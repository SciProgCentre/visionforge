package space.kscience.visionforge.solid

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.dataforge.meta.MutableMeta
import space.kscience.dataforge.meta.ObservableMutableMeta
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.dataforge.meta.get
import space.kscience.dataforge.names.*
import space.kscience.dataforge.values.Value
import space.kscience.visionforge.*


public interface SolidReference : VisionGroup {
    /**
     * The prototype for this reference.
     */
    public val prototype: Solid

    override fun getPropertyValue(
        name: Name,
        inherit: Boolean,
        includeStyles: Boolean,
        includeDefaults: Boolean
    ): Value? {
        meta[name]?.value?.let { return it }
        if (includeStyles) {
            getStyleProperty(name)?.let { return it }
        }
        prototype.getPropertyValue(name, inherit, includeStyles, includeDefaults)?.let { return it }
        if (inherit) {
            parent?.getPropertyValue(name, inherit, includeStyles, includeDefaults)?.let { return it }
        }
        return null
    }
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

    override var properties: MutableMeta? = null

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

    override fun getPropertyValue(
        name: Name,
        inherit: Boolean,
        includeStyles: Boolean,
        includeDefaults: Boolean
    ): Value? = super<SolidReference>.getPropertyValue(name, inherit, includeStyles, includeDefaults)

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
            if (refName.isEmpty()) {
                owner.prototype
            } else {
                val proto = (owner.prototype as? VisionGroup)?.get(refName)
                    ?: error("Prototype with name $refName not found in SolidReferenceGroup ${owner.refName}")
                proto as? Solid ?: error("Prototype with name $refName is ${proto::class} but expected Solid")
//                proto.unref as? Solid
//                    ?: error("Prototype with name $refName is ${proto::class} but expected Solid")
            }
        }

        override val meta: ObservableMutableMeta by lazy {
            owner.meta.getOrCreate(childToken(refName).asName())
        }

        override val children: Map<NameToken, Vision>
            get() = (prototype as? VisionGroup)?.children
                ?.filter { it.key != SolidGroup.PROTOTYPES_TOKEN }
                ?.mapValues { (key, _) ->
                    ReferenceChild(owner, refName + key.asName())
                } ?: emptyMap()

        override var parent: VisionGroup?
            get() {
                val parentName = refName.cutLast()
                return if (parentName.isEmpty()) owner else ReferenceChild(owner, parentName)
            }
            set(_) {
                error("Setting a parent for a reference child is not possible")
            }

        override fun invalidateProperty(propertyName: Name) {
            owner.invalidateProperty(childPropertyName(refName, propertyName))
        }

        override fun update(change: VisionChange) {
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

public fun SolidGroup.ref(
    templateName: String,
    name: String? = null,
): SolidReferenceGroup = ref(Name.parse(templateName), name)

/**
 * Add new [SolidReferenceGroup] wrapping given object and automatically adding it to the prototypes.
 * One must ensure that [prototypeHolder] is the owner of this group.
 */
public fun SolidGroup.newRef(
    name: String?,
    obj: Solid,
    prototypeHolder: PrototypeHolder = this,
    templateName: Name = Name.parse(name ?: obj.toString()),
): SolidReferenceGroup {
    val existing = getPrototype(templateName)
    if (existing == null) {
        prototypeHolder.prototypes {
            set(templateName, obj)
        }
    } else if (existing != obj) {
        error("Can't add different prototype on top of existing one")
    }
    return ref(templateName, name)
}
