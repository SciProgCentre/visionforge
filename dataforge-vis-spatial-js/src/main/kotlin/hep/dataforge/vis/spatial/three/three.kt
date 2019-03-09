package hep.dataforge.vis.spatial.three

import hep.dataforge.meta.MetaItem
import hep.dataforge.meta.float
import hep.dataforge.meta.get
import hep.dataforge.meta.node
import hep.dataforge.vis.DisplayObject
import hep.dataforge.vis.spatial.rotationOrder
import hep.dataforge.vis.spatial.rotationX
import hep.dataforge.vis.spatial.rotationY
import hep.dataforge.vis.spatial.rotationZ
import info.laht.threekt.core.Object3D
import info.laht.threekt.math.Euler
import info.laht.threekt.math.Vector3

/**
 * Utility methods for three.kt.
 * TODO move to three project
 */

@Suppress("FunctionName")
fun Group(children: Collection<Object3D>) = info.laht.threekt.objects.Group().apply {
    children.forEach { this.add(it) }
}

val DisplayObject.euler get() = Euler(rotationX, rotationY, rotationZ, rotationOrder.name)

val MetaItem<*>.vector get() = Vector3(node["x"].float ?: 0f, node["y"].float ?: 0f, node["z"].float ?: 0f)