package hep.dataforge.vis.spatial

import kotlin.math.PI

class Sphere(
    var radius: Float,
    var phiStart: Float = 0f,
    var phi: Float = PI2,
    var thetaStart: Float = 0f,
    var theta: Float = PI.toFloat()
) : VisualLeaf3D()

inline fun VisualGroup3D.sphere(
    radius: Number,
    phi: Number = 2 * PI,
    theta: Number = PI,
    name: String? = null,
    action: Sphere.() -> Unit = {}
) = Sphere(
    radius.toFloat(),
    phi = phi.toFloat(),
    theta = theta.toFloat()
).apply(action).also { set(name, it) }