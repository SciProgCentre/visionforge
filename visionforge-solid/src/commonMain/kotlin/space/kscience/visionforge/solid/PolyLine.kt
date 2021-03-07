package space.kscience.visionforge.solid

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.dataforge.meta.number
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.asName
import space.kscience.dataforge.names.plus
import space.kscience.visionforge.VisionBuilder
import space.kscience.visionforge.VisionContainerBuilder
import space.kscience.visionforge.allProperties
import space.kscience.visionforge.set

@Serializable
@SerialName("solid.line")
public class PolyLine(public var points: List<Point3D>) : SolidBase(), Solid {

    //var lineType by string()
    public var thickness: Number by allProperties(inherit = false).number(1.0,
        key = SolidMaterial.MATERIAL_KEY + THICKNESS_KEY)

    public companion object {
        public val THICKNESS_KEY: Name = "thickness".asName()
    }

}

@VisionBuilder
public fun VisionContainerBuilder<Solid>.polyline(
    vararg points: Point3D,
    name: String? = null,
    action: PolyLine.() -> Unit = {},
): PolyLine = PolyLine(points.toList()).apply(action).also { set(name, it) }