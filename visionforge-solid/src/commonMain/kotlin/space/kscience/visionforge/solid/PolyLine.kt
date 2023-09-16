package space.kscience.visionforge.solid

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.dataforge.meta.number
import space.kscience.visionforge.MutableVisionContainer
import space.kscience.visionforge.VisionBuilder
import space.kscience.visionforge.root
import space.kscience.visionforge.setChild

@Serializable
@SerialName("solid.line")
public class PolyLine(public val points: List<Float32Vector3D>) : SolidBase<PolyLine>() {

    //var lineType by string()
    public var thickness: Number by properties.root(inherit = false, includeStyles = true).number { DEFAULT_THICKNESS }

    public companion object {
        public const val DEFAULT_THICKNESS: Double = 1.0
    }
}

@VisionBuilder
public fun MutableVisionContainer<Solid>.polyline(
    vararg points: Float32Vector3D,
    name: String? = null,
    action: PolyLine.() -> Unit = {},
): PolyLine = PolyLine(points.toList()).apply(action).also { setChild(name, it) }