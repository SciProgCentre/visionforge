package space.kscience.visionforge.solid

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.visionforge.MutableVisionContainer
import space.kscience.visionforge.VisionBuilder
import space.kscience.visionforge.setChild

/**
 * Utility solids
 */
public abstract class MiscSolid: SolidBase<MiscSolid>()

/**
 * X-Y-Z axes
 */
@Serializable
@SerialName("solid.axes")
public class AxesSolid(public val size: Double): MiscSolid(){
    public companion object{
        public const val AXES_NAME: String = "@axes"
    }
}

@VisionBuilder
public fun MutableVisionContainer<Solid>.axes(
    size: Number,
    name: String = "@axes",
    block: AxesSolid.() -> Unit = {},
): AxesSolid = AxesSolid(size.toDouble()).apply(block).also {
    setChild(name, it)
}