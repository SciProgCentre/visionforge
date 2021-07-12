package space.kscience.visionforge.solid

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.descriptors.NodeDescriptor
import space.kscience.dataforge.meta.float
import space.kscience.dataforge.meta.get
import space.kscience.dataforge.meta.node
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

internal fun Meta.toVector(default: Float = 0f) = Point3D(
    this[Solid.X_KEY].float ?: default,
    this[Solid.Y_KEY].float ?: default,
    this[Solid.Z_KEY].float ?: default
)

internal fun Solid.updatePosition(meta: Meta?) {
    meta[Solid.POSITION_KEY].node?.toVector()?.let { position = it }
    meta[Solid.ROTATION_KEY].node?.toVector()?.let { rotation = it }
    meta[Solid.SCALE_KEY].node?.toVector(1f)?.let { scale = it }
}