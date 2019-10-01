package hep.dataforge.vis.spatial.gdml

import hep.dataforge.meta.update
import hep.dataforge.names.asName
import hep.dataforge.vis.common.MutableVisualGroup
import hep.dataforge.vis.spatial.Point3D
import hep.dataforge.vis.spatial.VisualGroup3D
import hep.dataforge.vis.spatial.VisualObject3D
import hep.dataforge.vis.spatial.plus
import kotlin.collections.component1
import kotlin.collections.component2

typealias GDMLOptimization = GDMLTransformer.(VisualGroup3D) -> VisualGroup3D

/**
 * Collapse nodes with single child
 */
val optimizeSingleChild: GDMLOptimization = { tree ->
    fun MutableVisualGroup.replaceChildren() {
        children.forEach { (key, child) ->
            if (child is VisualGroup3D && child.children.size == 1) {
                val newChild = child.children.values.first().apply {
                    config.update(child.config)
                }

                if (newChild is VisualObject3D) {
                    newChild.apply {
                        position += child.position
                        rotation += child.rotation
                        scale = when {
                            scale == null && child.scale == null -> null
                            scale == null -> child.scale
                            child.scale == null -> scale
                            else -> Point3D(
                                scale!!.x * child.scale!!.x,
                                scale!!.y * child.scale!!.y,
                                scale!!.z * child.scale!!.z
                            )
                        }
                    }
                }

                if (newChild is MutableVisualGroup) {
                    newChild.replaceChildren()
                }

                //actual replacement
                set(key.asName(), newChild)
            } else if (child is MutableVisualGroup) {
                child.replaceChildren()
            }
        }
    }

    tree.replaceChildren()

    tree
}