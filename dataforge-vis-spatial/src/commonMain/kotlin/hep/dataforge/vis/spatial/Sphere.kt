package hep.dataforge.vis.spatial

import hep.dataforge.vis.common.VisualObject
import hep.dataforge.vis.common.number
import kotlin.math.PI

class Sphere(parent: VisualObject?, var radius: Number) : VisualLeaf3D(parent) {
    var phiStart by number(0.0)
    var phi by number(2 * PI)
    var thetaStart by number(0.0)
    var theta by number(PI)
}

fun VisualGroup3D.sphere(
    radius: Number,
    phi: Number = 2 * PI,
    theta: Number = PI,
    name: String? = null,
    action: Sphere.() -> Unit = {}
) = Sphere(this, radius).apply(action).apply {
    this.phi = phi.toDouble()
    this.theta = theta.toDouble()
}.also { set(name, it) }