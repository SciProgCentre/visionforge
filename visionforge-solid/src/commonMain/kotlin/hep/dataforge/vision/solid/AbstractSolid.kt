package hep.dataforge.vision.solid

import hep.dataforge.vision.AbstractVision
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
public open class AbstractSolid: AbstractVision(), Solid {
    @Serializable(Point3DSerializer::class)
    override var position: Point3D? = null

    @Serializable(Point3DSerializer::class)
    override var rotation: Point3D? = null

    @Serializable(Point3DSerializer::class)
    override var scale: Point3D? = null
}