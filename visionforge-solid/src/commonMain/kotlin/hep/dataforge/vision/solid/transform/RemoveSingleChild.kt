package hep.dataforge.vision.solid.transform

import hep.dataforge.misc.DFExperimental
import hep.dataforge.names.asName
import hep.dataforge.vision.*
import hep.dataforge.vision.solid.*

@DFExperimental
internal fun mergeChild(parent: VisionGroup, child: Vision): Vision {
    return child.apply {

        configure(parent.meta)

        //parent.properties?.let { config.update(it) }

        if (this is Solid && parent is Solid) {
            position = (position ?: Point3D.ZERO) + (parent.position ?: Point3D.ZERO)
            rotation = (parent.rotation ?: Point3D.ZERO) + (parent.rotation ?: Point3D.ZERO)
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

@DFExperimental
internal object RemoveSingleChild : VisualTreeTransform<SolidGroup>() {

    override fun SolidGroup.transformInPlace() {
        fun MutableVisionGroup.replaceChildren() {
            children.forEach { (childName, parent) ->
                if (parent is SolidReferenceGroup) return@forEach //ignore refs
                if (parent is MutableVisionGroup) {
                    parent.replaceChildren()
                }
                if (parent is VisionGroup && parent.children.size == 1) {
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

    override fun SolidGroup.clone(): SolidGroup {
        TODO()
    }
}