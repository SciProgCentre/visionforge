package hep.dataforge.vision.solid

import hep.dataforge.vision.VisionBuilder
import hep.dataforge.vision.VisionContainerBuilder
import hep.dataforge.vision.set
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
    name: String = "",
    action: SolidLabel.() -> Unit = {},
): SolidLabel = SolidLabel(text, fontSize.toDouble(), fontFamily).apply(action).also { set(name, it) }