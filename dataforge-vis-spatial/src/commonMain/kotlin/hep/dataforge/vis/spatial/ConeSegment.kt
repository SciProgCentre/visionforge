@file:UseSerializers(Point3DSerializer::class)

package hep.dataforge.vis.spatial

import hep.dataforge.io.ConfigSerializer
import hep.dataforge.meta.Config
import hep.dataforge.vis.common.AbstractVisualObject
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

/**
 * A cylinder or cut cone segment
 */
@Serializable
class ConeSegment(
    var radius: Float,
    var height: Float,
    var upperRadius: Float,
    var startAngle: Float = 0f,
    var angle: Float = PI2
) : AbstractVisualObject(), VisualObject3D {

    @Serializable(ConfigSerializer::class)
    override var properties: Config? = null

    override var position: Point3D? = null
    override var rotation: Point3D? = null
    override var scale: Point3D? = null
}

inline fun VisualGroup3D.cylinder(
    r: Number,
    height: Number,
    name: String? = null,
    block: ConeSegment.() -> Unit = {}
): ConeSegment = ConeSegment(
    r.toFloat(),
    height.toFloat(),
    r.toFloat()
).apply(block).also { set(name, it) }