package hep.dataforge.vis.spatial

import hep.dataforge.meta.Meta
import hep.dataforge.vis.common.VisualGroup
import hep.dataforge.vis.common.VisualLeaf
import hep.dataforge.vis.common.VisualObject
import hep.dataforge.vis.common.double
import kotlin.math.PI

class Sphere(parent: VisualObject?, meta: Array<out Meta>) : VisualLeaf(parent, meta) {
    var radius by double(50.0)
    var phiStart by double(0.0)
    var phi by double(2 * PI)
    var thetaStart by double(0.0)
    var theta by double(PI)
}

fun VisualGroup.sphere(
    radius: Number,
    phi: Number = 2 * PI,
    theta: Number = PI,
    name: String? = null,
    vararg meta: Meta,
    action: Sphere.() -> Unit = {}
) = Sphere(this, meta).apply(action).apply {
    this.radius = radius.toDouble()
    this.phi = phi.toDouble()
    this.theta = theta.toDouble()
}.also { set(name, it) }