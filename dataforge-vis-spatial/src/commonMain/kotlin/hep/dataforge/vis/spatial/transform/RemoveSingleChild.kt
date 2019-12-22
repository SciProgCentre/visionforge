package hep.dataforge.vis.spatial.transform

import hep.dataforge.meta.update
import hep.dataforge.names.asName
import hep.dataforge.vis.common.MutableVisualGroup
import hep.dataforge.vis.common.VisualGroup
import hep.dataforge.vis.common.VisualObject
import hep.dataforge.vis.spatial.*

internal fun mergeChild(parent: VisualGroup, child: VisualObject): VisualObject {
    return child.apply {

        config.update(parent.config)

        //parent.properties?.let { config.update(it) }

        if (this is VisualObject3D && parent is VisualObject3D) {
            position += parent.position
            rotation += parent.rotation
            scale = when {
                scale == null && parent.scale == null -> null
                scale == null -> parent.scale
                parent.scale == null -> scale
                else -> Point3D(
                    scale!!.x * parent.scale!!.x,
                    scale!!.y * parent.scale!!.y,
                    scale!!.z * parent.scale!!.z
                )
            }
        }

    }
}

object RemoveSingleChild : VisualTreeTransform<VisualGroup3D>() {

    override fun VisualGroup3D.transformInPlace() {
        fun MutableVisualGroup.replaceChildren() {
            children.forEach { (childName, parent) ->
                if (parent is Proxy) return@forEach //ignore refs
                if (parent is MutableVisualGroup) {
                    parent.replaceChildren()
                }
                if (parent is VisualGroup && parent.children.size == 1) {
                    val child = parent.children.values.first()
                    val newParent = mergeChild(parent, child)
                    newParent.parent = null
                    set(childName.asName(), newParent)
                }
            }
        }

        replaceChildren()
        prototypes?.replaceChildren()
    }

    override fun VisualGroup3D.clone(): VisualGroup3D {
        TODO()
    }
}