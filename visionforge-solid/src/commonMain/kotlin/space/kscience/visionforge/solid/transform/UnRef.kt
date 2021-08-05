package space.kscience.visionforge.solid.transform

import space.kscience.dataforge.misc.DFExperimental
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.asName
import space.kscience.visionforge.MutableVisionGroup
import space.kscience.visionforge.VisionGroup
import space.kscience.visionforge.solid.SolidGroup
import space.kscience.visionforge.solid.SolidReferenceGroup

@DFExperimental
internal object UnRef : VisualTreeTransform<SolidGroup>() {
    private fun VisionGroup.countRefs(): Map<Name, Int> {
        return children.values.fold(HashMap()) { reducer, obj ->
            if (obj is VisionGroup) {
                val counter = obj.countRefs()
                counter.forEach { (key, value) ->
                    reducer[key] = (reducer[key] ?: 0) + value
                }
            } else if (obj is SolidReferenceGroup) {
                reducer[obj.refName] = (reducer[obj.refName] ?: 0) + 1
            }

            return reducer
        }
    }

    private fun MutableVisionGroup.unref(name: Name) {
        (this as? SolidGroup)?.prototypes{
            set(name, null)
        }
        children.filter { (it.value as? SolidReferenceGroup)?.refName == name }.forEach { (key, value) ->
            val reference = value as SolidReferenceGroup
            val newChild = reference.prototype.updateFrom(reference)
            newChild.parent = null
            set(key.asName(), newChild) // replace proxy with merged object
        }

        children.values.filterIsInstance<MutableVisionGroup>().forEach { it.unref(name) }
    }

    override fun SolidGroup.transformInPlace() {
        val counts = countRefs()
        counts.filter { it.value <= 1 }.forEach {
            this.unref(it.key)
        }
    }

    override fun SolidGroup.clone(): SolidGroup {
        TODO()
    }

}