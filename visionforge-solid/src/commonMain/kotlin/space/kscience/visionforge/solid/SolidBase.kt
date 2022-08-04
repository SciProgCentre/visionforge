package space.kscience.visionforge.solid

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.dataforge.meta.MutableMeta
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.dataforge.names.Name
import space.kscience.visionforge.AbstractVision
import space.kscience.visionforge.VisionChildren

@Serializable
@SerialName("solid")
public open class SolidBase : AbstractVision(), Solid {
    override val descriptor: MetaDescriptor get() = Solid.descriptor
    override val children: VisionChildren get() = VisionChildren.empty(this)

    override fun getProperty(
        name: Name,
        inherit: Boolean,
        includeStyles: Boolean,
        includeDefaults: Boolean,
    ): MutableMeta {
        return super<AbstractVision>.getProperty(name, inherit, includeStyles, includeDefaults)
    }
}
