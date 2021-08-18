package space.kscience.visionforge.solid

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.dataforge.meta.MutableMeta
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.NameToken
import space.kscience.visionforge.*

/**
 * A container with prototype support
 */
public interface PrototypeHolder {
    /**
     * Build or update prototype tree
     */
    @VisionBuilder
    public fun prototypes(builder: VisionContainerBuilder<Solid>.() -> Unit)

    /**
     * Resolve a prototype from this container. Should never return a ref.
     */
    public fun getPrototype(name: Name): Solid?
}

/**
 * Represents 3-dimensional Visual Group
 * @param prototypes A container for templates visible inside this group
 */
@Serializable
@SerialName("group.solid")
public class SolidGroup : VisionGroupBase(), Solid, PrototypeHolder {

    //FIXME to be removed after https://github.com/Kotlin/kotlinx.serialization/issues/1602 fix
    override var properties: MutableMeta? = null

    override val children: Map<NameToken, Vision> get() = super.childrenInternal.filter { it.key != PROTOTYPES_TOKEN }

    private var prototypes: MutableVisionGroup?
        get() = childrenInternal[PROTOTYPES_TOKEN] as? MutableVisionGroup
        set(value) {
            set(PROTOTYPES_TOKEN, value)
        }


    override val descriptor: MetaDescriptor get() = Solid.descriptor

    /**
     * Get a prototype redirecting the request to the parent if prototype is not found.
     * If prototype is a ref, then it is unfolded automatically.
     */
    override fun getPrototype(name: Name): Solid? =
        prototypes?.get(name)?.unref ?: (parent as? PrototypeHolder)?.getPrototype(name)

    /**
     * Create or edit prototype node as a group
     */
    override fun prototypes(builder: VisionContainerBuilder<Solid>.() -> Unit): Unit {
        (prototypes ?: SolidGroup().also {
            prototypes = it
        }).run(builder)
    }

    override fun createGroup(): SolidGroup = SolidGroup()

    override fun update(change: VisionChange) {
        updatePosition(change.properties)
        super.update(change)
    }

    public companion object {
        public val PROTOTYPES_TOKEN: NameToken = NameToken("@prototypes")
    }
}

@Suppress("FunctionName")
public fun SolidGroup(block: SolidGroup.() -> Unit): SolidGroup {
    return SolidGroup().apply(block)
}

@VisionBuilder
public fun VisionContainerBuilder<Vision>.group(
    name: Name? = null,
    action: SolidGroup.() -> Unit = {},
): SolidGroup = SolidGroup().apply(action).also { set(name, it) }

/**
 * Define a group with given [name], attach it to this parent and return it.
 */
@VisionBuilder
public fun VisionContainerBuilder<Vision>.group(name: String, action: SolidGroup.() -> Unit = {}): SolidGroup =
    SolidGroup().apply(action).also { set(name, it) }
