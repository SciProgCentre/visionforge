package space.kscience.visionforge.solid

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.visionforge.MutableVisionContainer


public sealed class StlSolid: SolidBase<StlSolid>()

@Serializable
@SerialName("solid.stl.url")
public class StlUrlSolid(public val url: String) : StlSolid()

@Serializable
@SerialName("solid.stl.binary")
public class StlBinarySolid(public val data: ByteArray) : StlSolid()

public inline fun MutableVisionContainer<Solid>.stl(
    url: String,
    name: String? = null,
    action: StlSolid.() -> Unit = {},
): StlSolid = StlUrlSolid(url).apply(action).also { 
    setVision(SolidGroup.inferNameFor(name, it), it)
}