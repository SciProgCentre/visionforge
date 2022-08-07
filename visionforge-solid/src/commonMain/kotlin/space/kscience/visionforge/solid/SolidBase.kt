package space.kscience.visionforge.solid

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.visionforge.AbstractVision

@Serializable
@SerialName("solid")
public open class SolidBase<T : Solid> : AbstractVision(), Solid {
    override val descriptor: MetaDescriptor get() = Solid.descriptor
}
