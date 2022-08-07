package space.kscience.visionforge.solid

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.dataforge.meta.number
import space.kscience.visionforge.*

@Serializable
@SerialName("solid.line")
public class PolyLine(public val points: List<Point3D>) : SolidBase<PolyLine>() {

    //var lineType by string()
    public var thickness: Number by properties[SolidMaterial.MATERIAL_KEY].number { 1.0 }
}

@VisionBuilder
public fun MutableVisionContainer<Solid>.polyline(
    vararg points: Point3D,
    name: String? = null,
    action: PolyLine.() -> Unit = {},
): PolyLine = PolyLine(points.toList()).apply(action).also { set(name, it) }