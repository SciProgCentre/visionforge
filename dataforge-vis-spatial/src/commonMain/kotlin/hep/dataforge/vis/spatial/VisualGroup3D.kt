@file:UseSerializers(
    Point3DSerializer::class,
    ConfigSerializer::class,
    NameTokenSerializer::class,
    NameSerializer::class,
    MetaSerializer::class
)

package hep.dataforge.vis.spatial

import hep.dataforge.io.serialization.ConfigSerializer
import hep.dataforge.io.serialization.MetaSerializer
import hep.dataforge.io.serialization.NameSerializer
import hep.dataforge.meta.Config
import hep.dataforge.meta.Meta
import hep.dataforge.names.Name
import hep.dataforge.names.NameToken
import hep.dataforge.names.asName
import hep.dataforge.names.isEmpty
import hep.dataforge.vis.common.AbstractVisualGroup
import hep.dataforge.vis.common.VisualGroup
import hep.dataforge.vis.common.VisualObject
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlin.collections.set

@Serializable
class VisualGroup3D : AbstractVisualGroup(), VisualObject3D {
    /**
     * A container for templates visible inside this group
     */
    @SerialName(PROTOTYPES_KEY)
    var prototypes: VisualGroup3D? = null
        set(value) {
            value?.parent = this
            field = value
        }

    //FIXME to be lifted to AbstractVisualGroup after https://github.com/Kotlin/kotlinx.serialization/issues/378 is fixed
    override var properties: Config? = null
    override val styleSheet = HashMap<Name, Meta>()

    override var position: Point3D? = null
    override var rotation: Point3D? = null
    override var scale: Point3D? = null

    @SerialName("children")
    private val _children = HashMap<NameToken, VisualObject>()
    override val children: Map<NameToken, VisualObject> get() = _children

    override fun removeChild(token: NameToken) {
        _children.remove(token)
        childrenChanged(token.asName(), null)
    }

    override fun setChild(token: NameToken, child: VisualObject) {
        if (child.parent == null) {
            child.parent = this
        } else {
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

    fun getPrototype(name: Name): VisualObject3D? =
        prototypes?.get(name) as? VisualObject3D ?: (parent as? VisualGroup3D)?.getPrototype(name)

    fun setPrototype(name: Name, obj: VisualObject3D, attachToParent: Boolean = false) {
        val parent = this.parent
        if (attachToParent && parent is VisualGroup3D) {
            parent.setPrototype(name, obj, attachToParent)
        } else {
            (prototypes ?: VisualGroup3D().also { this.prototypes = it }).set(name, obj)
        }
    }

    companion object {
        const val PROTOTYPES_KEY = "templates"
    }
}

/**
 * A fix for serialization bug that writes all proper parents inside the tree after deserialization
 */
fun VisualGroup.attachChildren() {
    this.children.values.forEach {
        it.parent = this
        (it as? VisualGroup)?.attachChildren()
    }
    if (this is VisualGroup3D) {
        prototypes?.also {
            it.parent = this
            it.attachChildren()
        }
    }
}

fun VisualGroup3D.group(key: String = "", action: VisualGroup3D.() -> Unit = {}): VisualGroup3D =
    VisualGroup3D().apply(action).also { set(key, it) }