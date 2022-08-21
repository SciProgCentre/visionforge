package space.kscience.visionforge.solid.transform

import space.kscience.dataforge.misc.DFExperimental
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.asName
import space.kscience.visionforge.root
import space.kscience.visionforge.solid.*

private operator fun Number.plus(other: Number) = toFloat() + other.toFloat()
private operator fun Number.times(other: Number) = toFloat() * other.toFloat()

@DFExperimental
internal fun Solid.updateFrom(other: Solid): Solid {
    x += other.x
    y += other.y
    z += other.y
    if(quaternion != null || other.quaternion != null) TODO("Quaternion support not implemented")
    rotationX += other.rotationX
    rotationY += other.rotationY
    rotationZ += other.rotationZ
    scaleX *= other.scaleX
    scaleY *= other.scaleY
    scaleZ *= other.scaleZ
    properties.setProperty(Name.EMPTY, other.properties.root())
    return this
}


@DFExperimental
internal object RemoveSingleChild : VisualTreeTransform<SolidGroup>() {

    override fun SolidGroup.transformInPlace() {
        fun SolidGroup.replaceChildren() {
            items.forEach { (childName, parent) ->
                if (parent is SolidReference) return@forEach //ignore refs
                if (parent is SolidGroup) {
                    parent.replaceChildren()
                }
                if (parent is SolidGroup && parent.items.size == 1) {
                    val child: Solid = parent.items.values.first()
                    val newParent = child.updateFrom(parent)
                    newParent.parent = null
                    children.setChild(childName.asName(), newParent)
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