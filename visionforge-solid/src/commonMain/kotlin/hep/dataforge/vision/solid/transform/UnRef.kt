package hep.dataforge.vision.solid.transform

import hep.dataforge.names.Name
import hep.dataforge.names.asName
import hep.dataforge.vision.MutableVisionGroup
import hep.dataforge.vision.VisionGroup
import hep.dataforge.vision.solid.Proxy
import hep.dataforge.vision.solid.SolidGroup

object UnRef : VisualTreeTransform<SolidGroup>() {
    private fun VisionGroup.countRefs(): Map<Name, Int> {
        return children.values.fold(HashMap()) { reducer, obj ->
            if (obj is VisionGroup) {
                val counter = obj.countRefs()
                counter.forEach { (key, value) ->
                    reducer[key] = (reducer[key] ?: 0) + value
                }
            } else if (obj is Proxy) {
                reducer[obj.templateName] = (reducer[obj.templateName] ?: 0) + 1
            }

            return reducer
        }
    }

    private fun MutableVisionGroup.unref(name: Name) {
        (this as? SolidGroup)?.prototypes?.set(name, null)
        children.filter { (it.value as? Proxy)?.templateName == name }.forEach { (key, value) ->
            val proxy = value as Proxy
            val newChild = mergeChild(proxy, proxy.prototype)
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