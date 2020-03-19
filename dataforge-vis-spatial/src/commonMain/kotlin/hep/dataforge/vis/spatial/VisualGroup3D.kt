@file:UseSerializers(
    Point3DSerializer::class
)

package hep.dataforge.vis.spatial

import hep.dataforge.meta.Config
import hep.dataforge.names.Name
import hep.dataforge.names.NameToken
import hep.dataforge.names.asName
import hep.dataforge.names.isEmpty
import hep.dataforge.vis.AbstractVisualGroup
import hep.dataforge.vis.StyleSheet
import hep.dataforge.vis.VisualObject
import hep.dataforge.vis.set
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlin.collections.set

/**
 * Represents 3-dimensional Visual Group
 */
@Serializable
@SerialName("group.3d")
class VisualGroup3D : AbstractVisualGroup(), VisualObject3D {

    override var styleSheet: StyleSheet? = null
        private set

    /**
     * A container for templates visible inside this group
     */
    var prototypes: VisualGroup3D? = null
        set(value) {
            value?.parent = this
            field = value
        }

    //FIXME to be lifted to AbstractVisualGroup after https://github.com/Kotlin/kotlinx.serialization/issues/378 is fixed
    override var properties: Config? = null

    override var position: Point3D? = null
    override var rotation: Point3D? = null
    override var scale: Point3D? = null

    @SerialName("children")
    private val _children = HashMap<NameToken, VisualObject>()
    override val children: Map<NameToken, VisualObject> get() = _children

//    init {
//        //Do after deserialization
//        attachChildren()
//    }

    override fun attachChildren() {
        prototypes?.parent = this
        prototypes?.attachChildren()
        super.attachChildren()
    }

    /**
     * Update or create stylesheet
     */
    fun styleSheet(block: StyleSheet.() -> Unit) {
        val res = styleSheet ?: StyleSheet(this).also { styleSheet = it }
        res.block()
    }

    override fun removeChild(token: NameToken) {
        _children.remove(token)?.run { parent = null }
        childrenChanged(token.asName(), null)
    }

    override fun setChild(token: NameToken, child: VisualObject) {
        if (child.parent == null) {
            child.parent = this
        } else if (child.parent !== this) {
            error("Can't reassign existing parent for $child")
        }
        _children[token] = child
        childrenChanged(token.asName(), child)
    }

//    /**
//     * TODO add special static group to hold statics without propagation
//     */
//    override fun addStatic(child: VisualObject) = setChild(NameToken("@static(${child.hashCode()})"), child)

    override fun createGroup(name: Name): VisualGroup3D {
        return when {
            name.isEmpty() -> error("Should be unreachable")
            name.length == 1 -> {
                val token = name.first()!!
                when (val current = children[token]) {
                    null -> VisualGroup3D().also { setChild(token, it) }
                    is VisualGroup3D -> current
                    else -> error("Can't create group with name $name because it exists and not a group")
                }
            }
            else -> createGroup(name.first()!!.asName()).createGroup(name.cutFirst())
        }
    }

    companion object {
//        val PROTOTYPES_KEY = NameToken("@prototypes")

        fun fromJson(json: String): VisualGroup3D =
            Visual3D.json.parse(serializer(), json).also { it.attachChildren() }
    }
}

/**
 * Ger a prototype redirecting the request to the parent if prototype is not found
 */
tailrec fun VisualGroup3D.getPrototype(name: Name): VisualObject3D? =
    prototypes?.get(name) as? VisualObject3D ?: (parent as? VisualGroup3D)?.getPrototype(name)

/**
 * Create or edit prototype node as a group
 */
inline fun VisualGroup3D.prototypes(builder: VisualGroup3D.() -> Unit): Unit {
    (prototypes ?: VisualGroup3D().also { prototypes = it }).run(builder)
}

/**
 * Define a group with given [key], attach it to this parent and return it.
 */
fun VisualGroup3D.group(key: String = "", action: VisualGroup3D.() -> Unit = {}): VisualGroup3D =
    VisualGroup3D().apply(action).also {
        set(key, it)
    }

