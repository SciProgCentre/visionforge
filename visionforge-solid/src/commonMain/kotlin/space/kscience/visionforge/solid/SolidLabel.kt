package space.kscience.visionforge.solid

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.visionforge.MutableVisionContainer
import space.kscience.visionforge.VisionBuilder
import space.kscience.visionforge.setChild

@Serializable
@SerialName("solid.label")
public class SolidLabel(
    public val text: String,
    public val fontSize: Double,
    public val fontFamily: String,
) : SolidBase<SolidLabel>()

@VisionBuilder
public fun MutableVisionContainer<Solid>.label(
    text: String,
    fontSize: Number = 20,
    fontFamily: String = "Arial",
    name: String? = null,
    action: SolidLabel.() -> Unit = {},
): SolidLabel = SolidLabel(text, fontSize.toDouble(), fontFamily).apply(action).also { setChild(name, it) }