package space.kscience.visionforge.solid

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.visionforge.VisionBuilder
import space.kscience.visionforge.VisionContainerBuilder
import space.kscience.visionforge.set

@Serializable
@SerialName("solid.label")
public class SolidLabel(
    public var text: String,
    public var fontSize: Double,
    public var fontFamily: String,
) : SolidBase(), Solid

@VisionBuilder
public fun VisionContainerBuilder<Solid>.label(
    text: String,
    fontSize: Number = 20,
    fontFamily: String = "Arial",
    name: String? = null,
    action: SolidLabel.() -> Unit = {},
): SolidLabel = SolidLabel(text, fontSize.toDouble(), fontFamily).apply(action).also { set(name, it) }