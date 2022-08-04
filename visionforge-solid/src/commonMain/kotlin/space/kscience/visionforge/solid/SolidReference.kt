package space.kscience.visionforge.solid

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.names.*
import space.kscience.dataforge.values.Value
import space.kscience.visionforge.*
import space.kscience.visionforge.solid.SolidReference.Companion.REFERENCE_CHILD_PROPERTY_PREFIX


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

/**
 * @param name A name of reference child relative to prototype root
 */
internal class SolidReferenceChild(
    val owner: SolidReference,
    override var parent: Vision?,
    val childName: Name,
) : Solid {

    val prototype: Solid
        get() = owner.prototype.children[childName] as? Solid
            ?: error("Prototype with name $childName not found")

    override val meta: Meta get() = owner.getProperty(childToken(childName).asName())

    override fun getPropertyValue(
        name: Name,
        inherit: Boolean,
        includeStyles: Boolean,
        includeDefaults: Boolean,
    ): Value? {
        owner.getPropertyValue(
            childPropertyName(childName, name), inherit, includeStyles, includeDefaults
        )?.let { return it }
        if (includeStyles) {
            getStyleProperty(name)?.value?.let { return it }
        }
        prototype.getPropertyValue(name, inherit, includeStyles, includeDefaults)?.let { return it }
        if (inherit) {
            parent?.getPropertyValue(name, inherit, includeStyles, includeDefaults)?.let { return it }
        }
        return null
    }

    override fun setProperty(name: Name, node: Meta?) {
        owner.setProperty(childPropertyName(childName, name), node)
    }

    override fun setPropertyValue(name: Name, value: Value?) {
        owner.setPropertyValue(childPropertyName(childName, name), value)
    }

    override val propertyChanges: SharedFlow<Name>
        get() = TODO("Not yet implemented")

    override fun invalidateProperty(propertyName: Name) {
        owner.invalidateProperty(childPropertyName(childName, propertyName))
    }

    override fun update(change: VisionChange) {
        TODO("Not yet implemented")
    }


    override val children: VisionChildren = object : VisionChildren {
        override val parent: Vision get() = this@SolidReferenceChild

        override val keys: Set<NameToken> get() = prototype.children.keys

        override val changes: Flow<Name> get() = emptyFlow()

        override fun get(token: NameToken): SolidReferenceChild? {
            if (token !in prototype.children.keys) return null
            return SolidReferenceChild(this@SolidReferenceChild.owner, this@SolidReferenceChild, childName + token)
        }
    }

    companion object {

        private fun childToken(childName: Name): NameToken =
            NameToken(REFERENCE_CHILD_PROPERTY_PREFIX, childName.toString())

        private fun childPropertyName(childName: Name, propertyName: Name): Name =
            childToken(childName) + propertyName

    }
}

@Serializable
@SerialName("solid.ref")
public class SolidReference(
    @SerialName("prototype") public val prototypeName: Name,
) : SolidBase() {

    /**
     * The prototype for this reference.
     */
    public val prototype: Solid by lazy {
        //Recursively search for defined template in the parent
        if (parent == null) error("No parent is present for SolidReference")
        if (parent !is PrototypeHolder) error("Parent does not hold prototypes")
        (parent as? PrototypeHolder)?.getPrototype(prototypeName)
            ?: error("Prototype with name $prototypeName not found")
    }

    override val children: VisionChildren
        get() = object : VisionChildren {
            override val parent: Vision get() = this@SolidReference

            override val keys: Set<NameToken> get() = prototype.children.keys

            override val changes: Flow<Name> get() = emptyFlow()

            override fun get(token: NameToken): SolidReferenceChild? {
                if (token !in prototype.children.keys) return null
                return SolidReferenceChild(this@SolidReference, this@SolidReference, token.asName())
            }
        }

    public companion object{
        public const val REFERENCE_CHILD_PROPERTY_PREFIX: String = "@child"
    }
}

/**
 * Create ref for existing prototype
 */
public fun VisionContainerBuilder<Solid>.ref(
    templateName: Name,
    name: String? = null,
): SolidReference = SolidReference(templateName).also { set(name, it) }

public fun VisionContainerBuilder<Solid>.ref(
    templateName: String,
    name: String? = null,
): SolidReference = ref(Name.parse(templateName), name)

/**
 * Add new [SolidReference] wrapping given object and automatically adding it to the prototypes.
 */
public fun SolidGroup.newRef(
    name: String?,
    obj: Solid,
    prototypeHolder: SolidGroup = this,
    prototypeName: Name = Name.parse(name ?: obj.toString()),
): SolidReference {
    val existing = prototypeHolder.getPrototype(prototypeName)
    if (existing == null) {
        prototypeHolder.prototypes {
            set(prototypeName, obj)
        }
    } else if (existing != obj) {
        error("Can't add different prototype on top of existing one")
    }
    return children.ref(prototypeName, name)
}


//
//
///**
// * A reference [Solid] to reuse a template object
// */
//@Serializable
//@SerialName("solid.ref")
//public class SolidReferenceGroup(
//    public val refName: Name,
//) : VisionGroup(), SolidReference, VisionGroup<Solid>, Solid {
//
//    /**
//     * Recursively search for defined template in the parent
//     */
//    override val prototype: Solid by lazy {
//        if (parent == null) error("No parent is present for SolidReferenceGroup")
//        if (parent !is PrototypeHolder) error("Parent does not hold prototypes")
//        (parent as? PrototypeHolder)?.getPrototype(refName) ?: error("Prototype with name $refName not found")
//    }
//
//    override val items: Map<NameToken, VisionGroupItem<Solid>>
//        get() = (prototype as? VisionGroup<*>)?.items
//            ?.filter { it.key != SolidGroup.PROTOTYPES_TOKEN }
//            ?.mapValues {
//                VisionGroupItem.Node(ReferenceChild(this, it.key.asName()))
//            } ?: emptyMap()
//
//    override val descriptor: MetaDescriptor get() = prototype.descriptor
//
//
//    /**
//     * A ProxyChild is created temporarily only to interact with properties, it does not store any values
//     * (properties are stored in external cache) and created and destroyed on-demand).
//     */
//    private class ReferenceChild(
//        val owner: SolidReferenceGroup,
//        private val refName: Name,
//    ) : SolidReference, VisionGroup<Solid>, Solid {
//
//        override val prototype: Solid by lazy {
//            if (refName.isEmpty()) {
//                owner.prototype
//            } else {
//                val proto = (owner.prototype).children.get(refName)
//                    ?: error("Prototype with name $refName not found in SolidReferenceGroup ${owner.refName}")
//                proto as? Solid ?: error("Prototype with name $refName is ${proto::class} but expected Solid")
////                proto.unref as? Solid
////                    ?: error("Prototype with name $refName is ${proto::class} but expected Solid")
//            }
//        }
//
//        override val meta: ObservableMutableMeta by lazy {
//            owner.meta.getOrCreate(childToken(refName).asName())
//        }
//
//        override val items: Map<NameToken, VisionGroupItem<Solid>>
//            get() = (prototype as? VisionGroup<*>)?.items
//                ?.filter { it.key != SolidGroup.PROTOTYPES_TOKEN }
//                ?.mapValues { (key, _) ->
//                    VisionGroupItem.Node(ReferenceChild(owner, refName + key.asName()))
//                } ?: emptyMap()
//
//        override var parent: VisionGroup<*>?
//            get() {
//                val parentName = refName.cutLast()
//                return if (parentName.isEmpty()) owner else ReferenceChild(owner, parentName)
//            }
//            set(_) {
//                error("Setting a parent for a reference child is not possible")
//            }
//
//        override fun invalidateProperty(propertyName: Name) {
//            owner.invalidateProperty(childPropertyName(refName, propertyName))
//        }
//
//        override fun update(change: VisionChange) {
//            change.properties?.let {
//                updateProperties(it, Name.EMPTY)
//            }
//        }
//
//        override val descriptor: MetaDescriptor get() = prototype.descriptor
//
//    }
//
//    public companion object {
//        public const val REFERENCE_CHILD_PROPERTY_PREFIX: String = "@child"
//    }
//}
