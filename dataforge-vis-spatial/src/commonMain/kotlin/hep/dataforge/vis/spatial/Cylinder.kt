package hep.dataforge.vis.spatial

import hep.dataforge.vis.common.VisualObject
import kotlin.math.PI

/**
 * A cylinder or cut cone segment
 */
class Cylinder(
    parent: VisualObject?,
    var radius: Number,
    var height: Number,
    var upperRadius: Number = radius,
    var startAngle: Number = 0f,
    var angle: Number = 2 * PI
) : VisualLeaf3D(parent)

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