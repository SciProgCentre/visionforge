package hep.dataforge.vision.solid

import hep.dataforge.meta.MetaItem
import hep.dataforge.meta.descriptors.NodeDescriptor
import hep.dataforge.names.Name
import hep.dataforge.names.NameToken
import hep.dataforge.vision.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

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
    @Serializable(Prototypes.Companion::class) @SerialName("prototypes") private var prototypes: MutableVisionGroup? = null,
) : VisionGroupBase(), Solid, PrototypeHolder {

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

    override var position: Point3D? = null

    override var rotation: Point3D? = null

    override var scale: Point3D? = null

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
//        val PROTOTYPES_KEY = NameToken("@prototypes")
    }
}

@Suppress("FunctionName")
public fun SolidGroup(block: SolidGroup.() -> Unit): SolidGroup {
    return SolidGroup().apply(block)
}

@VisionBuilder
public fun VisionContainerBuilder<Vision>.group(
    name: Name = Name.EMPTY,
    action: SolidGroup.() -> Unit = {},
): SolidGroup =
    SolidGroup().apply(action).also { set(name, it) }

/**
 * Define a group with given [name], attach it to this parent and return it.
 */
@VisionBuilder
public fun VisionContainerBuilder<Vision>.group(name: String, action: SolidGroup.() -> Unit = {}): SolidGroup =
    SolidGroup().apply(action).also { set(name, it) }

/**
 * A special class which works as a holder for prototypes
 */
@Serializable(Prototypes.Companion::class)
internal class Prototypes(
    children: Map<NameToken, Vision> = emptyMap(),
) : VisionGroupBase(children as? MutableMap<NameToken, Vision> ?: children.toMutableMap()), PrototypeHolder {

    init {
        //used during deserialization only
        children.values.forEach {
            it.parent = parent
        }
    }

    override fun getOwnProperty(name: Name): MetaItem? = null

    override fun getProperty(
        name: Name,
        inherit: Boolean,
        includeStyles: Boolean,
        includeDefaults: Boolean,
    ): MetaItem? = null

    override fun setProperty(name: Name, item: MetaItem?, notify: Boolean) {
        error("Can't ser property of prototypes container")
    }

    override val descriptor: NodeDescriptor? = null

    companion object : KSerializer<MutableVisionGroup> {

        private val mapSerializer: KSerializer<Map<NameToken, Vision>> =
            MapSerializer(
                NameToken.serializer(),
                PolymorphicSerializer(Vision::class)
            )

        override val descriptor: SerialDescriptor get() = mapSerializer.descriptor

        override fun deserialize(decoder: Decoder): MutableVisionGroup {
            val map = mapSerializer.deserialize(decoder)
            return Prototypes(map)
        }

        override fun serialize(encoder: Encoder, value: MutableVisionGroup) {
            mapSerializer.serialize(encoder, value.children)
        }
    }

    override fun prototypes(builder: VisionContainerBuilder<Solid>.() -> Unit) {
        apply(builder)
    }

    override fun getPrototype(name: Name): Solid? = get(name) as? Solid
}
