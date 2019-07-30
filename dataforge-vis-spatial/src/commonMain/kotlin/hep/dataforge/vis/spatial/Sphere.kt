package hep.dataforge.vis.spatial

import hep.dataforge.meta.Meta
import hep.dataforge.vis.common.VisualGroup
import hep.dataforge.vis.common.VisualObject
import hep.dataforge.vis.common.number
import kotlin.math.PI

class Sphere(parent: VisualObject?, var radius: Number, meta: Array<out Meta>) : VisualLeaf3D(parent, meta) {
    var phiStart by number(0.0)
    var phi by number(2 * PI)
    var thetaStart by number(0.0)
    var theta by number(PI)
}

fun VisualGroup.sphere(
    radius: Number,
    phi: Number = 2 * PI,
    theta: Number = PI,
    name: String? = null,
    vararg meta: Meta,
    action: Sphere.() -> Unit = {}
) = Sphere(this, radius, meta).apply(action).apply {
    this.phi = phi.toDouble()
    this.theta = theta.toDouble()
}.also { set(name, it) }