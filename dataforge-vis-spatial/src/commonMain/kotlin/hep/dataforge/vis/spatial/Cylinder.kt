package hep.dataforge.vis.spatial

/**
 * A cylinder or cut cone segment
 */
class Cylinder(
    var radius: Float,
    var height: Float,
    var upperRadius: Float = radius,
    var startAngle: Float = 0f,
    var angle: Float = PI2
) : VisualLeaf3D()

inline fun VisualGroup3D.cylinder(
    r: Number,
    height: Number,
    name: String? = null,
    block: Cylinder.() -> Unit = {}
): Cylinder = Cylinder(
    r.toFloat(),
    height.toFloat()
).apply(block).also { set(name, it) }