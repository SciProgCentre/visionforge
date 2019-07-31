package hep.dataforge.vis.spatial

import hep.dataforge.vis.common.VisualObject
import hep.dataforge.vis.common.number
import kotlin.math.PI

/**
 * A cylinder or cut cone segment
 */
class Cylinder(parent: VisualObject?, var radius: Number, var height: Number) : VisualLeaf3D(parent) {
    var upperRadius by number(radius)
    var startAngle by number(0.0)
    var angle by number(2 * PI)
}

fun VisualGroup3D.cylinder(
    r: Number,
    height: Number,
    name: String? = null,
    block: Cylinder.() -> Unit = {}
): Cylinder {
    val cylinder = Cylinder(this, r, height)
    cylinder.apply(block)
    return cylinder.also { set(name, it) }
}