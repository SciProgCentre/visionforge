package space.kscience.visionforge.solid

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.visionforge.MutableVisionContainer
import space.kscience.visionforge.VisionBuilder
import space.kscience.visionforge.setChild


public sealed class StlSolid: SolidBase<StlSolid>()

@Serializable
@SerialName("solid.stl.url")
public class StlUrlSolid(public val url: String) : StlSolid()

@Serializable
@SerialName("solid.stl.binary")
public class StlBinarySolid(public val data: ByteArray) : StlSolid()

@VisionBuilder
public inline fun MutableVisionContainer<Solid>.stl(
    url: String,
    name: String? = null,
    action: StlSolid.() -> Unit = {},
): StlSolid = StlUrlSolid(url).apply(action).also { setChild(name, it) }