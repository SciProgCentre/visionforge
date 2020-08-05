package hep.dataforge.vision.spatial.transform

import hep.dataforge.meta.update
import hep.dataforge.names.asName
import hep.dataforge.vision.MutableVisualGroup
import hep.dataforge.vision.VisualGroup
import hep.dataforge.vision.VisualObject
import hep.dataforge.vision.spatial.*

internal fun mergeChild(parent: VisualGroup, child: VisualObject): VisualObject {
    return child.apply {

        config.update(parent.config)

        //parent.properties?.let { config.update(it) }

        if (this is VisualObject3D && parent is VisualObject3D) {
            position = (position ?: World.ZERO) + (parent.position ?: World.ZERO)
            rotation = (parent.rotation ?: World.ZERO) + (parent.rotation ?: World.ZERO)
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
        prototypes {
            replaceChildren()
        }
    }

    override fun VisualGroup3D.clone(): VisualGroup3D {
        TODO()
    }
}