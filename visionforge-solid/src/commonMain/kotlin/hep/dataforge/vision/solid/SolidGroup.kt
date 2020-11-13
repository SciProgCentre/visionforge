package hep.dataforge.vision.solid

import hep.dataforge.meta.Config
import hep.dataforge.meta.descriptors.NodeDescriptor
import hep.dataforge.names.Name
import hep.dataforge.names.NameToken
import hep.dataforge.vision.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

public interface PrototypeHolder {
    public val parent: VisionGroup?
    public val prototypes: MutableVisionGroup?
}

/**
 * Represents 3-dimensional Visual Group
 */
@Serializable
@SerialName("group.solid")
public class SolidGroup : VisionGroupBase(), Solid, PrototypeHolder {

    override val descriptor: NodeDescriptor get() = Solid.descriptor

    /**
     * A container for templates visible inside this group
     */
    @Serializable(PrototypesSerializer::class)
    override var prototypes: MutableVisionGroup? = null
        private set

    /**
     * Create or edit prototype node as a group
     */
    public fun prototypes(builder: MutableVisionGroup.() -> Unit): Unit {
        (prototypes ?: Prototypes().also {
            prototypes = it
            it.parent = this
        }).run(builder)
    }

    @Serializable(Point3DSerializer::class)
    override var position: Point3D? = null

    @Serializable(Point3DSerializer::class)
    override var rotation: Point3D? = null

    @Serializable(Point3DSerializer::class)
    override var scale: Point3D? = null

    override fun attachChildren() {
        prototypes?.parent = this
        prototypes?.attachChildren()
        super.attachChildren()
    }


//    /**
//     * TODO add special static group to hold statics without propagation
//     */
//    override fun addStatic(child: VisualObject) = setChild(NameToken("@static(${child.hashCode()})"), child)

    override fun createGroup(): SolidGroup = SolidGroup()


    public companion object {
//        val PROTOTYPES_KEY = NameToken("@prototypes")
    }
}

@Suppress("FunctionName")
public fun SolidGroup(block: SolidGroup.() -> Unit): SolidGroup {
    return SolidGroup().apply(block)
}

/**
 * Ger a prototype redirecting the request to the parent if prototype is not found
 */
public tailrec fun PrototypeHolder.getPrototype(name: Name): Solid? =
    prototypes?.get(name) as? Solid ?: (parent as? PrototypeHolder)?.getPrototype(name)

public fun MutableVisionGroup.group(name: Name = Name.EMPTY, action: SolidGroup.() -> Unit = {}): SolidGroup =
    SolidGroup().apply(action).also { set(name, it) }

/**
 * Define a group with given [name], attach it to this parent and return it.
 */
public fun MutableVisionGroup.group(name: String, action: SolidGroup.() -> Unit = {}): SolidGroup =
    SolidGroup().apply(action).also { set(name, it) }

/**
 * A special class which works as a holder for prototypes
 */
@Serializable(PrototypesSerializer::class)
internal class Prototypes(
    override var children: MutableMap<NameToken, Vision> = LinkedHashMap(),
) : VisionGroupBase(), PrototypeHolder {

    override fun styleSheet(block: StyleSheet.() -> Unit) {
        error("Can't define stylesheet for prototypes block")
    }

    override var properties: Config?
        get() = null
        set(_) {
            error("Can't define properties for prototypes block")
        }

    override val prototypes: MutableVisionGroup get() = this

    override fun attachChildren() {
        children.values.forEach {
            it.parent = parent
            (it as? VisionGroup)?.attachChildren()
        }
    }
}
