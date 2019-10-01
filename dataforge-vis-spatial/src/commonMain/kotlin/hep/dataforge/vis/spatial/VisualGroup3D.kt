@file:UseSerializers(
    Point3DSerializer::class,
    ConfigSerializer::class,
    NameTokenSerializer::class,
    NameSerializer::class
)

package hep.dataforge.vis.spatial

import hep.dataforge.io.ConfigSerializer
import hep.dataforge.io.NameSerializer
import hep.dataforge.meta.Config
import hep.dataforge.meta.Meta
import hep.dataforge.meta.MetaBuilder
import hep.dataforge.meta.set
import hep.dataforge.names.*
import hep.dataforge.vis.common.AbstractVisualGroup
import hep.dataforge.vis.common.AbstractVisualObject
import hep.dataforge.vis.common.VisualGroup
import hep.dataforge.vis.common.VisualObject
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

@Serializable
class VisualGroup3D : AbstractVisualGroup(), VisualObject3D {
    /**
     * A container for templates visible inside this group
     */
    var templates: VisualGroup3D? = null
        set(value) {
            value?.parent = this
            field = value
        }

    //FIXME to be lifted to AbstractVisualGroup after https://github.com/Kotlin/kotlinx.serialization/issues/378 is fixed
    public override var properties: Config? = null

    private val styles = HashMap<Name, Meta>()

    override fun getStyle(name: Name): Meta? = styles[name]

    override fun setStyle(name: Name, meta: Meta) {
        fun VisualObject.applyStyle(name: Name, meta: Meta) {
            if (style.contains(name.toString())) {
                //full update
                //TODO do a fine grained update
                if(this is AbstractVisualObject){
                    styleChanged()
                } else {
                    propertyChanged(EmptyName)
                }
            }
            if (this is VisualGroup) {
                this.children.forEach { (_, child) ->
                    child.applyStyle(name, meta)
                }
            }
        }
        styles[name] = meta
        applyStyle(name, meta)
    }

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

    /**
     * TODO add special static group to hold statics without propagation
     */
    override fun addStatic(child: VisualObject) = setChild(NameToken("@static(${child.hashCode()})"), child)

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

    fun getTemplate(name: Name): VisualObject3D? =
        templates?.get(name) as? VisualObject3D
            ?: (parent as? VisualGroup3D)?.getTemplate(name)

    override fun MetaBuilder.updateMeta() {
        set(TEMPLATES_KEY, templates?.toMeta())
        updatePosition()
        updateChildren()
    }

    companion object {
        const val TEMPLATES_KEY = "templates"
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
        templates?.apply {
            parent = this@attachChildren
            attachChildren()
        }
    }
}

fun VisualGroup3D.group(key: String = "", action: VisualGroup3D.() -> Unit = {}): VisualGroup3D =
    VisualGroup3D().apply(action).also { set(key, it) }
