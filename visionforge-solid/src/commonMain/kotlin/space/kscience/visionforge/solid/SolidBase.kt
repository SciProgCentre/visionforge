package space.kscience.visionforge.solid

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.dataforge.meta.MutableMeta
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.visionforge.VisionBase
import space.kscience.visionforge.VisionChange

@Serializable
@SerialName("solid")
public open class SolidBase : VisionBase(), Solid {
    //FIXME to be removed after https://github.com/Kotlin/kotlinx.serialization/issues/1602 fix
    override var properties: MutableMeta? = null

    override val descriptor: MetaDescriptor get() = Solid.descriptor

    override fun update(change: VisionChange) {
        updatePosition(change.properties)
        super.update(change)
    }
}
