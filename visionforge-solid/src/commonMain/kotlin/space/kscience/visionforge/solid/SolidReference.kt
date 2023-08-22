package space.kscience.visionforge.solid

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import space.kscience.dataforge.meta.*
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.dataforge.meta.descriptors.get
import space.kscience.dataforge.names.*
import space.kscience.visionforge.*
import space.kscience.visionforge.AbstractVisionGroup.Companion.updateProperties
import space.kscience.visionforge.solid.SolidReference.Companion.REFERENCE_CHILD_PROPERTY_PREFIX


/**
 * Get a vision prototype if it is a [SolidReference] or vision itself if it is not.
 * Unref is recursive, so it always returns a non-reference.
 */
public val Vision.prototype: Solid
    get() = when (this) {
        is SolidReference -> prototype.prototype
        is SolidReferenceChild -> prototype.prototype
        is Solid -> this
        else -> error("This Vision is neither Solid nor SolidReference")
    }

@Serializable
@SerialName("solid.ref")
public class SolidReference(
    @SerialName("prototype") public val prototypeName: Name,
) : VisionGroup, Solid {

    @Transient
    override var parent: Vision? = null

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
    override val descriptor: MetaDescriptor get() = prototype.descriptor

    @SerialName("properties")
    private var propertiesInternal: MutableMeta? = null

    override val properties: MutableVisionProperties by lazy {
        object : AbstractVisionProperties(this) {
            override var properties: MutableMeta?
                get() = propertiesInternal
                set(value) {
                    propertiesInternal = value
                }

            override fun getValue(name: Name, inherit: Boolean?, includeStyles: Boolean?): Value? {
                if (name == Vision.STYLE_KEY) {
                    return buildList {
                        properties?.getValue(Vision.STYLE_KEY)?.list?.forEach {
                            add(it)
                        }
                        prototype.styles.forEach {
                            add(it.asValue())
                        }
                    }.distinct().asValue()
                }
                //1. resolve own properties
                properties?.getValue(name)?.let { return it }

                val descriptor = descriptor?.get(name)
                val inheritFlag = inherit ?: descriptor?.inherited ?: false
                val stylesFlag = includeStyles ?: descriptor?.usesStyles ?: true

                //2. Resolve prototype onw properties
                prototype.properties.own?.getValue(name)?.let { return it }

                if (stylesFlag) {
                    //3. own styles
                    own?.getValue(Vision.STYLE_KEY)?.list?.forEach { styleName ->
                        getStyle(styleName.string)?.getValue(name)?.let { return it }
                    }
                    //4. prototype styles
                    prototype.getStyleProperty(name)?.value?.let { return it }
                }

                if (inheritFlag) {
                    //5. own inheritance
                    parent?.properties?.getValue(name, inheritFlag, includeStyles)?.let { return it }
                    //6. prototype inheritance
                    prototype.parent?.properties?.getValue(name, inheritFlag, includeStyles)?.let { return it }
                }

                return descriptor?.defaultValue
            }


            override fun invalidate(propertyName: Name) {
                //send update signal
                @OptIn(DelicateCoroutinesApi::class)
                (manager?.context ?: GlobalScope).launch {
                    changesInternal.emit(propertyName)
                }

                // update styles
                if (propertyName == Vision.STYLE_KEY) {
                    styles.asSequence()
                        .mapNotNull { getStyle(it) }
                        .flatMap { it.items.asSequence() }
                        .distinctBy { it.key }
                        .forEach {
                            invalidate(it.key.asName())
                        }
                }
            }
        }
    }

    override val children: VisionChildren
        get() = object : VisionChildren {
            override val parent: Vision get() = this@SolidReference

            override val keys: Set<NameToken> get() = prototype.children?.keys ?: emptySet()

            override val changes: Flow<Name> get() = emptyFlow()

            override fun get(token: NameToken): SolidReferenceChild? {
                if (token !in (prototype.children?.keys ?: emptySet())) return null
                return SolidReferenceChild(this@SolidReference, this@SolidReference, token.asName())
            }
        }

    public companion object {
        public const val REFERENCE_CHILD_PROPERTY_PREFIX: String = "@child"
    }
}

/**
 * @param childName A name of reference child relative to prototype root
 */
internal class SolidReferenceChild(
    val owner: SolidReference,
    override var parent: Vision?,
    val childName: Name,
) : Solid, VisionGroup {

    val prototype: Solid
        get() = owner.prototype.children?.getChild(childName) as? Solid
            ?: error("Prototype with name $childName not found")

    override val descriptor: MetaDescriptor get() = prototype.descriptor

    @Transient
    override val properties: MutableVisionProperties = object : MutableVisionProperties {
        override val descriptor: MetaDescriptor get() = this@SolidReferenceChild.descriptor

        override val own: MutableMeta by lazy { owner.properties.getProperty(childToken(childName).asName()) }

        override fun getValue(
            name: Name,
            inherit: Boolean?,
            includeStyles: Boolean?,
        ): Value? = own.getValue(name) ?: prototype.properties.getValue(name, inherit, includeStyles)

        override fun setProperty(name: Name, node: Meta?, notify: Boolean) {
            own.setMeta(name, node)
        }

        override fun setValue(name: Name, value: Value?, notify: Boolean) {
            own.setValue(name, value)
        }

        override val changes: Flow<Name> get() = owner.properties.changes.filter { it.startsWith(childToken(childName)) }

        override fun invalidate(propertyName: Name) {
            owner.properties.invalidate(childPropertyName(childName, propertyName))
        }
    }

    override fun update(change: VisionChange) {
        change.children?.forEach { (name, change) ->
            when {
                change.vision == NullVision -> error("Deleting children inside ref is not allowed.")
                change.vision != null -> error("Updating content of the ref is not allowed")
                else -> children.getChild(name)?.update(change)
            }
        }
        change.properties?.let {
            updateProperties(it, Name.EMPTY)
        }
    }


    override val children: VisionChildren = object : VisionChildren {
        override val parent: Vision get() = this@SolidReferenceChild

        override val keys: Set<NameToken> get() = prototype.children?.keys ?: emptySet()

        override val changes: Flow<Name> get() = emptyFlow()

        override fun get(token: NameToken): SolidReferenceChild? {
            if (token !in (prototype.children?.keys ?: emptySet())) return null
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

/**
 * Create ref for existing prototype
 */
public fun MutableVisionContainer<Solid>.ref(
    templateName: Name,
    name: Name? = null,
): SolidReference = SolidReference(templateName).also { setChild(name, it) }

public fun MutableVisionContainer<Solid>.ref(
    templateName: Name,
    name: String? = null,
): SolidReference = ref(templateName, name?.parseAsName())

public fun MutableVisionContainer<Solid>.ref(
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
            setChild(prototypeName, obj)
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
