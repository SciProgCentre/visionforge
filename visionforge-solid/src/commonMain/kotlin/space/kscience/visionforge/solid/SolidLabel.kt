package space.kscience.visionforge.solid

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.visionforge.VisionBuilder
import space.kscience.visionforge.VisionContainerBuilder
import space.kscience.visionforge.VisionPropertyContainer
import space.kscience.visionforge.set

@Serializable
@SerialName("solid.label")
public class SolidLabel(
    public val text: String,
    public val fontSize: Double,
    public val fontFamily: String,
) : SolidBase(), VisionPropertyContainer<SolidLabel>

@VisionBuilder
public fun VisionContainerBuilder<Solid>.label(
    text: String,
    fontSize: Number = 20,
    fontFamily: String = "Arial",
    name: String? = null,
    action: SolidLabel.() -> Unit = {},
): SolidLabel = SolidLabel(text, fontSize.toDouble(), fontFamily).apply(action).also { set(name, it) }