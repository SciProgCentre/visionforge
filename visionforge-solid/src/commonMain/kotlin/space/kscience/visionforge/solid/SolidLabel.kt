package space.kscience.visionforge.solid

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.visionforge.MutableVisionContainer

@Serializable
@SerialName("solid.label")
public class SolidLabel(
    public val text: String,
    public val fontSize: Double,
    public val fontFamily: String,
) : SolidBase<SolidLabel>()

public fun MutableVisionContainer<Solid>.label(
    text: String,
    fontSize: Number = 20,
    fontFamily: String = "Arial",
    name: String? = null,
    action: SolidLabel.() -> Unit = {},
): SolidLabel = SolidLabel(text, fontSize.toDouble(), fontFamily).apply(action).also { 
    setVision(SolidGroup.inferNameFor(name, it), it)
}