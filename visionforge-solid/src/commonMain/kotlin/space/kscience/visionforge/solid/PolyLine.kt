package space.kscience.visionforge.solid

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.asName
import space.kscience.dataforge.names.plus
import space.kscience.visionforge.*

@Serializable
@SerialName("solid.line")
public class PolyLine(public val points: List<Point3D>) : SolidBase(), VisionPropertyContainer<PolyLine> {

    //var lineType by string()
    public var thickness: Number by numberProperty(name = SolidMaterial.MATERIAL_KEY + THICKNESS_KEY) { 1.0 }


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