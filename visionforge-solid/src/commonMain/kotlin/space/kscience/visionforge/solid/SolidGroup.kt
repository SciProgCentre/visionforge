package space.kscience.visionforge.solid

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.NameToken
import space.kscience.dataforge.names.parseAsName
import space.kscience.visionforge.AbstractVisionGroup
import space.kscience.visionforge.MutableVisionContainer
import space.kscience.visionforge.MutableVisionGroup
import space.kscience.visionforge.VisionBuilder


/**
 * A container with prototype support
 */
public interface PrototypeHolder {
    /**
     * Build or update the prototype tree
     */
    @VisionBuilder
    public fun prototypes(builder: MutableVisionContainer<Solid>.() -> Unit)

    /**
     * Resolve a prototype from this container. Should never return a ref.
     */
    public fun getPrototype(name: Name): Solid?
}


/**
 * A [Solid] group with additional accessor methods
 */
@Serializable
@SerialName("group.solid")
public class SolidGroup : AbstractVisionGroup(), Solid, PrototypeHolder, MutableVisionGroup,
    MutableVisionContainer<Solid> {

    public val items: Map<NameToken, Solid>
        get() = children.keys.mapNotNull {
            val value = children[it] as? Solid ?: return@mapNotNull null
            it to value
        }.toMap()

    /**
     * Get a child solid with given relative [name] if it exists
     */
    public operator fun get(name: Name): Solid? = children.getChild(name) as? Solid

    private var prototypes: SolidGroup?
        get() = items[PROTOTYPES_TOKEN] as? SolidGroup
        set(value) {
            children[PROTOTYPES_TOKEN] = value
        }


    override val descriptor: MetaDescriptor get() = Solid.descriptor

    /**
     * Get a prototype redirecting the request to the parent if prototype is not found.
     * If a prototype is a ref, then it is unfolded automatically.
     */
    override fun getPrototype(name: Name): Solid? =
        prototypes?.get(name)?.prototype ?: (parent as? PrototypeHolder)?.getPrototype(name)

    /**
     * Create or edit prototype node as a group
     */
    override fun prototypes(builder: MutableVisionContainer<Solid>.() -> Unit): Unit {
        (prototypes ?: SolidGroup().also { prototypes = it }).children.run(builder)
    }

    override fun createGroup(): SolidGroup = SolidGroup()
//
//    override fun update(change: VisionChange) {
//        updatePosition(change.properties)
//        super.update(change)
//    }

    override fun setChild(name: Name?, child: Solid?) {
        children.setChild(name, child)
    }

    public companion object {
        public val PROTOTYPES_TOKEN: NameToken = NameToken("@prototypes")
    }
}

public operator fun SolidGroup.get(name:String): Solid? = get(name.parseAsName())

@VisionBuilder
public inline fun MutableVisionContainer<Solid>.solidGroup(
    name: Name? = null,
    builder: SolidGroup.() -> Unit = {},
): SolidGroup = SolidGroup().also { setChild(name, it) }.apply(builder)
//root first, update later

/**
 * Define a group with given [name], attach it to this parent and return it.
 */
@VisionBuilder
public inline fun MutableVisionContainer<Solid>.solidGroup(
    name: String,
    action: SolidGroup.() -> Unit = {},
): SolidGroup = solidGroup(name.parseAsName(), action)

/**
 * Create a [SolidGroup] using given configuration [block]
 */
public inline fun SolidGroup(block: SolidGroup.() -> Unit): SolidGroup = SolidGroup().apply(block)