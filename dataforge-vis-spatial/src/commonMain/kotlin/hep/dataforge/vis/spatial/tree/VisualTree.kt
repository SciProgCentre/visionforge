package hep.dataforge.vis.spatial.tree

import hep.dataforge.vis.common.VisualObject

interface VisualTree<out T: VisualObject> {
    val item: T?
    val children: Collection<VisualTree<T>>
}