package hep.dataforge.vis.spatial.transform

import hep.dataforge.names.Name
import hep.dataforge.names.asName
import hep.dataforge.vis.common.MutableVisualGroup
import hep.dataforge.vis.common.VisualGroup
import hep.dataforge.vis.spatial.Proxy
import hep.dataforge.vis.spatial.VisualGroup3D

object UnRef : VisualTreeTransform<VisualGroup3D>() {
    private fun VisualGroup.countRefs(): Map<Name, Int> {
        return children.values.fold(HashMap()) { reducer, obj ->
            if (obj is VisualGroup) {
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

    private fun MutableVisualGroup.unref(name: Name) {
        (this as? VisualGroup3D)?.templates?.set(name, null)
        children.filter { (it.value as? Proxy)?.templateName == name }.forEach { (key, value) ->
            val proxy = value as Proxy
            val newChild = mergeChild(proxy, proxy.prototype)
            newChild.parent = null
            set(key.asName(), newChild) // replace proxy with merged object
        }

        children.values.filterIsInstance<MutableVisualGroup>().forEach { it.unref(name) }
    }

    override fun VisualGroup3D.transformInPlace() {
        val counts = countRefs()
        counts.filter { it.value <= 1 }.forEach {
            this.unref(it.key)
        }
    }

    override fun VisualGroup3D.clone(): VisualGroup3D {
        TODO()
    }

}