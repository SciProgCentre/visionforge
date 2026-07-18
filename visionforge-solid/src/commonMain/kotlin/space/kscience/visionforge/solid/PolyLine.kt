package space.kscience.visionforge.solid

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.dataforge.meta.number
import space.kscience.dataforge.names.NameToken
import space.kscience.visionforge.MutableVisionContainer

@Serializable
@SerialName("solid.line")
public class PolyLine(public val points: List<FloatVector3D>) : SolidBase<PolyLine>() {

    //var lineType by string()
    public var thickness: Number by properties.number { DEFAULT_THICKNESS }

    public companion object {
        public const val DEFAULT_THICKNESS: Double = 1.0
    }
}

public fun MutableVisionContainer<Solid>.polyline(
    vararg points: FloatVector3D,
    name: String? = null,
    action: PolyLine.() -> Unit = {},
): PolyLine = PolyLine(points.toList()).apply(action).also {
    setVision(name?.let(NameToken::parse) ?: SolidGroup.staticNameFor(it), it)
}