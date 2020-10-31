package hep.dataforge.vision.solid

import hep.dataforge.meta.*
import hep.dataforge.vision.AbstractVision
import hep.dataforge.vision.Vision
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
@SerialName("solid")
public open class BasicSolid: AbstractVision(), Solid {
    @Serializable(Point3DSerializer::class)
    override var position: Point3D? = null

    @Serializable(Point3DSerializer::class)
    override var rotation: Point3D? = null

    @Serializable(Point3DSerializer::class)
    override var scale: Point3D? = null

    override fun update(meta: Meta) {
        fun Meta.toVector(default: Float = 0f) = Point3D(
            this[Solid.X_KEY].float ?: default,
            this[Solid.Y_KEY].float ?: default,
            this[Solid.Z_KEY].float ?: default
        )

        meta[Solid.POSITION_KEY].node?.toVector()?.let { position = it }
        meta[Solid.ROTATION].node?.toVector()?.let { rotation = it }
        meta[Solid.SCALE_KEY].node?.toVector(1f)?.let { scale = it }
        super.update(meta)
    }
}