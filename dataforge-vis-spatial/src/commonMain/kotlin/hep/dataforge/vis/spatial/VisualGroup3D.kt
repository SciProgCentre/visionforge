@file:UseSerializers(Point3DSerializer::class, ConfigSerializer::class, NameTokenSerializer::class)
package hep.dataforge.vis.spatial

import hep.dataforge.io.ConfigSerializer
import hep.dataforge.meta.Config
import hep.dataforge.meta.Configurable
import hep.dataforge.meta.MetaBuilder
import hep.dataforge.meta.set
import hep.dataforge.names.Name
import hep.dataforge.names.NameToken
import hep.dataforge.names.asName
import hep.dataforge.names.isEmpty
import hep.dataforge.vis.common.AbstractVisualGroup
import hep.dataforge.vis.common.VisualObject
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
class VisualGroup3D : AbstractVisualGroup(), VisualObject3D, Configurable {
    /**
     * A container for templates visible inside this group
     */
    var templates: VisualGroup3D? = null
        set(value) {
            value?.parent = this
            field = value
        }

    @Serializable(ConfigSerializer::class)
    override var properties: Config? = null

    override var position: Point3D? = null
    override var rotation: Point3D? = null
    override var scale: Point3D? = null

    private val _children = HashMap<NameToken, VisualObject>()
    override val children: Map<NameToken, VisualObject> get() = _children

    override fun removeChild(token: NameToken) {
        _children.remove(token)
    }

    override fun setChild(token: NameToken, child: VisualObject?) {
        if (child == null) {
            _children.remove(token)
        } else {
            if (child.parent == null) {
                child.parent = this
            } else {
                error("Can't reassign existing parent for $child")
            }
            _children[token] = child
        }
    }

    /**
     * TODO add special static group to hold statics without propagation
     */
    override fun addStatic(child: VisualObject) = setChild(NameToken(child.hashCode().toString()), child)

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

fun VisualGroup3D.group(key: String = "", action: VisualGroup3D.() -> Unit = {}): VisualGroup3D =
    VisualGroup3D().apply(action).also { set(key, it) }
