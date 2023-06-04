package space.kscience.visionforge.solid

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.visionforge.MutableVisionContainer
import space.kscience.visionforge.VisionBuilder
import space.kscience.visionforge.setChild


public sealed class StlVision: SolidBase<StlVision>()

@Serializable
@SerialName("solid.stl.url")
public class StlUrlVision(public val url: String) : StlVision()

@Serializable
@SerialName("solid.stl.binary")
public class StlBinaryVision(public val data: ByteArray) : StlVision()

@VisionBuilder
public inline fun MutableVisionContainer<Solid>.stl(
    url: String,
    name: String? = null,
    action: StlVision.() -> Unit = {},
): StlVision = StlUrlVision(url).apply(action).also { setChild(name, it) }