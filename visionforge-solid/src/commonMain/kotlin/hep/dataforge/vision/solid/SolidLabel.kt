@file:UseSerializers(Point3DSerializer::class)

package hep.dataforge.vision.solid

import hep.dataforge.meta.Config
import hep.dataforge.vision.AbstractVision
import hep.dataforge.vision.MutableVisionGroup
import hep.dataforge.vision.VisionContainerBuilder
import hep.dataforge.vision.set
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
@SerialName("solid.label")
public class SolidLabel(public var text: String, public var fontSize: Double, public var fontFamily: String) : AbstractVision(), Solid {
    override var properties: Config? = null

    override var position: Point3D? = null
    override var rotation: Point3D? = null
    override var scale: Point3D? = null

}

public fun VisionContainerBuilder<Solid>.label(
    text: String,
    fontSize: Number = 20,
    fontFamily: String = "Arial",
    name: String = "",
    action: SolidLabel.() -> Unit = {},
): SolidLabel = SolidLabel(text, fontSize.toDouble(), fontFamily).apply(action).also { set(name, it) }