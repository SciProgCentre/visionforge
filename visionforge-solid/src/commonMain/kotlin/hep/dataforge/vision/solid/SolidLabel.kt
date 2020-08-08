@file:UseSerializers(Point3DSerializer::class)

package hep.dataforge.vision.solid

import hep.dataforge.meta.Config
import hep.dataforge.vision.AbstractVision
import hep.dataforge.vision.MutableVisionGroup
import hep.dataforge.vision.set
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
@SerialName("solid.label")
class SolidLabel(var text: String, var fontSize: Double, var fontFamily: String) : AbstractVision(), Solid {
    override var properties: Config? = null

    override var position: Point3D? = null
    override var rotation: Point3D? = null
    override var scale: Point3D? = null

}

fun MutableVisionGroup.label(
    text: String,
    fontSize: Number = 20,
    fontFamily: String = "Arial",
    name: String = "",
    action: SolidLabel.() -> Unit = {}
) =
    SolidLabel(text, fontSize.toDouble(), fontFamily).apply(action).also { set(name, it) }