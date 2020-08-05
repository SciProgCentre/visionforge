@file:UseSerializers(
    Point3DSerializer::class
)

package hep.dataforge.vision.spatial

import hep.dataforge.meta.Config
import hep.dataforge.names.Name
import hep.dataforge.names.NameToken
import hep.dataforge.names.asName
import hep.dataforge.vision.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlin.collections.set

interface PrototypeHolder {
    val parent: VisualGroup?
    val prototypes: MutableVisualGroup?
}

/**
 * Represents 3-dimensional Visual Group
 */
@Serializable
@SerialName("group.3d")
class VisualGroup3D : AbstractVisualGroup(), VisualObject3D, PrototypeHolder {

    override var styleSheet: StyleSheet? = null

    /**
     * A container for templates visible inside this group
     */
    @Serializable(PrototypesSerializer::class)
    override var prototypes: MutableVisualGroup? = null
        private set

    /**
     * Create or edit prototype node as a group
     */
    fun prototypes(builder: MutableVisualGroup.() -> Unit): Unit {
        (prototypes ?: Prototypes().also {
            prototypes = it
            attach(it)
        }).run(builder)
    }

    //FIXME to be lifted to AbstractVisualGroup after https://github.com/Kotlin/kotlinx.serialization/issues/378 is fixed
    override var ownProperties: Config? = null

    override var position: Point3D? = null
    override var rotation: Point3D? = null
    override var scale: Point3D? = null

    @SerialName("children")
    private val _children = HashMap<NameToken, VisualObject>()
    override val children: Map<NameToken, VisualObject> get() = _children

    override fun attachChildren() {
        prototypes?.parent = this
        prototypes?.attachChildren()
        super.attachChildren()
    }

    override fun removeChild(token: NameToken) {
        _children.remove(token)?.apply { parent = null }
    }

    override fun setChild(token: NameToken, child: VisualObject) {
        _children[token] = child
    }

//    /**
//     * TODO add special static group to hold statics without propagation
//     */
//    override fun addStatic(child: VisualObject) = setChild(NameToken("@static(${child.hashCode()})"), child)

    override fun createGroup(): VisualGroup3D = VisualGroup3D()


    companion object {
//        val PROTOTYPES_KEY = NameToken("@prototypes")

        fun parseJson(json: String): VisualGroup3D =
            Visual3D.json.parse(serializer(), json).also { it.attachChildren() }
    }
}

/**
 * Ger a prototype redirecting the request to the parent if prototype is not found
 */
tailrec fun PrototypeHolder.getPrototype(name: Name): VisualObject3D? =
    prototypes?.get(name) as? VisualObject3D ?: (parent as? PrototypeHolder)?.getPrototype(name)

/**
 * Define a group with given [name], attach it to this parent and return it.
 */
fun MutableVisualGroup.group(name: String = "", action: VisualGroup3D.() -> Unit = {}): VisualGroup3D =
    VisualGroup3D().apply(action).also {
        set(name, it)
    }

internal class Prototypes(
    override var children: MutableMap<NameToken, VisualObject> = LinkedHashMap()
) : AbstractVisualGroup(), MutableVisualGroup, PrototypeHolder {

    override var styleSheet: StyleSheet?
        get() = null
        set(_) {
            error("Can't define stylesheet for prototypes block")
        }

    override fun removeChild(token: NameToken) {
        children.remove(token)
        childrenChanged(token.asName(), null)
    }

    override fun setChild(token: NameToken, child: VisualObject) {
        children[token] = child
    }

    override fun createGroup() = SimpleVisualGroup()

    override var ownProperties: Config?
        get() = null
        set(_) {
            error("Can't define properties for prototypes block")
        }

    override val prototypes: MutableVisualGroup get() = this

    override fun attachChildren() {
        children.values.forEach {
            it.parent = parent
            (it as? VisualGroup)?.attachChildren()
        }
    }
}
