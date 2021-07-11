package space.kscience.visionforge.solid.transform

import space.kscience.dataforge.misc.DFExperimental
import space.kscience.dataforge.names.asName
import space.kscience.visionforge.*
import space.kscience.visionforge.solid.*

@DFExperimental
internal fun mergeChild(parent: VisionGroup, child: Vision): Vision {
    return child.apply {

        configure(parent.meta)

        //parent.properties?.let { config.update(it) }

        if (this is Solid && parent is Solid) {
            position += parent.position
            rotation += parent.rotation
            scale = Point3D(
                scale.x * parent.scale.x,
                scale.y * parent.scale.y,
                scale.z * parent.scale.z
            )

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