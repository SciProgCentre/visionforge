package space.kscience.visionforge.solid

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.dataforge.meta.descriptors.NodeDescriptor
import space.kscience.visionforge.VisionBase
import space.kscience.visionforge.VisionChange

@Serializable
@SerialName("solid")
public open class SolidBase : VisionBase(), Solid {
    override val descriptor: NodeDescriptor get() = Solid.descriptor

    override fun update(change: VisionChange) {
        updatePosition(change.properties)
        super.update(change)
    }
}
