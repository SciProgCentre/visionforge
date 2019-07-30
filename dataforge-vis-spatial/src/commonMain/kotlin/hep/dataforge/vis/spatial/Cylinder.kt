package hep.dataforge.vis.spatial

import hep.dataforge.meta.Meta
import hep.dataforge.vis.common.VisualGroup
import hep.dataforge.vis.common.VisualObject
import hep.dataforge.vis.common.number
import kotlin.math.PI

/**
 * A cylinder or cut cone segment
 */
class Cylinder(parent: VisualObject?, var radius: Number, var height: Number, meta: Array<out Meta>) :
    VisualLeaf3D(parent, meta) {
    var upperRadius by number(radius)
    var startAngle by number(0.0)
    var angle by number(2 * PI)
}

fun VisualGroup.cylinder(
    r: Number,
    height: Number,
    name: String? = null,
    vararg meta: Meta,
    block: Cylinder.() -> Unit = {}
): Cylinder {
    val cylinder = Cylinder(this, r, height, meta)
    cylinder.apply(block)
    return cylinder.also { set(name, it) }
}