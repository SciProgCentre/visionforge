package space.kscience.visionforge.solid

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.visionforge.VisionBase

@Serializable
@SerialName("solid")
public open class SolidBase : VisionBase(), Solid {
    override val descriptor: MetaDescriptor get() = Solid.descriptor
}
