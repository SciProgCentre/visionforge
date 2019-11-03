@file:UseSerializers(Point3DSerializer::class)

package hep.dataforge.vis.spatial

import hep.dataforge.io.serialization.ConfigSerializer
import hep.dataforge.meta.Config
import hep.dataforge.vis.common.AbstractVisualObject
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlin.math.PI

@Serializable
class Sphere(
    var radius: Float,
    var phiStart: Float = 0f,
    var phi: Float = PI2,
    var thetaStart: Float = 0f,
    var theta: Float = PI.toFloat()
) : AbstractVisualObject(), VisualObject3D {

    @Serializable(ConfigSerializer::class)
    override var properties: Config? = null

    override var position: Point3D? = null
    override var rotation: Point3D? = null
    override var scale: Point3D? = null
}

inline fun VisualGroup3D.sphere(
    radius: Number,
    phi: Number = 2 * PI,
    theta: Number = PI,
    name: String = "",
    action: Sphere.() -> Unit = {}
) = Sphere(
    radius.toFloat(),
    phi = phi.toFloat(),
    theta = theta.toFloat()
).apply(action).also { set(name, it) }