package space.kscience.visionforge.solid

import kotlinx.serialization.KSerializer
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import space.kscience.dataforge.meta.MetaItem
import space.kscience.dataforge.meta.descriptors.NodeDescriptor
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.NameToken
import space.kscience.visionforge.*

public interface PrototypeHolder {
    @VisionBuilder
    public fun prototypes(builder: VisionContainerBuilder<Solid>.() -> Unit)

    public fun getPrototype(name: Name): Solid?
}

/**
 * Represents 3-dimensional Visual Group
 * @param prototypes A container for templates visible inside this group
 */
@Serializable
@SerialName("group.solid")
public class SolidGroup(
    @Serializable(PrototypeSerializer::class) internal var prototypes: MutableVisionGroup? = null,
//    override var position: Point3D? = null,
//    override var rotation: Point3D? = null,
//    override var scale: Point3D? = null,
) : VisionGroupBase(), Solid, PrototypeHolder {

    init {
        prototypes?.parent = this
    }

    override val descriptor: NodeDescriptor get() = Solid.descriptor

    /**
     * Ger a prototype redirecting the request to the parent if prototype is not found
     */
    override fun getPrototype(name: Name): Solid? =
        (prototypes?.get(name) as? Solid) ?: (parent as? PrototypeHolder)?.getPrototype(name)

    /**
     * Create or edit prototype node as a group
     */
    override fun prototypes(builder: VisionContainerBuilder<Solid>.() -> Unit): Unit {
        (prototypes ?: Prototypes().also {
            prototypes = it
            it.parent = this
        }).run(builder)
    }

//    /**
//     * TODO add special static group to hold statics without propagation
//     */
//    override fun addStatic(child: VisualObject) = setChild(NameToken("@static(${child.hashCode()})"), child)

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

/**
 * A special class which works as a holder for prototypes
 */
internal class Prototypes(
    children: MutableMap<NameToken, Vision> = hashMapOf(),
) : VisionGroupBase(children), PrototypeHolder {

    override fun getProperty(
        name: Name,
        inherit: Boolean,
        includeStyles: Boolean,
        includeDefaults: Boolean,
    ): MetaItem? = null

    override fun setProperty(name: Name, item: MetaItem?, notify: Boolean) {
        error("Can't set property of a prototypes container")
    }

    override val descriptor: NodeDescriptor? = null

    override fun prototypes(builder: VisionContainerBuilder<Solid>.() -> Unit) {
        apply(builder)
    }

    override fun getPrototype(name: Name): Solid? = get(name) as? Solid
}

internal class PrototypeSerializer : KSerializer<MutableVisionGroup> {

    private val mapSerializer: KSerializer<Map<NameToken, Vision>> =
        MapSerializer(
            NameToken.serializer(),
            PolymorphicSerializer(Vision::class)
        )

    override val descriptor: SerialDescriptor get() = mapSerializer.descriptor

    override fun deserialize(decoder: Decoder): MutableVisionGroup {
        val map = mapSerializer.deserialize(decoder)
        return Prototypes(map.toMutableMap())
    }

    override fun serialize(encoder: Encoder, value: MutableVisionGroup) {
        mapSerializer.serialize(encoder, value.children)
    }
}