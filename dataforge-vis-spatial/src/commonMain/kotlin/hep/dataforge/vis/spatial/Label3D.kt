@file:UseSerializers(Point3DSerializer::class)

package hep.dataforge.vis.spatial

import hep.dataforge.io.serialization.ConfigSerializer
import hep.dataforge.meta.Config
import hep.dataforge.vis.common.AbstractVisualObject
import hep.dataforge.vis.common.set
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
class Label3D(var text: String, var fontSize: Double, var fontFamily: String) : AbstractVisualObject(),
    VisualObject3D {
    @Serializable(ConfigSerializer::class)
    override var properties: Config? = null

    override var position: Point3D? = null
    override var rotation: Point3D? = null
    override var scale: Point3D? = null

}

fun VisualGroup3D.label(
    text: String,
    fontSize: Number = 20,
    fontFamily: String = "Arial",
    name: String = "",
    action: Label3D.() -> Unit = {}
) =
    Label3D(text, fontSize.toDouble(), fontFamily).apply(action).also { set(name, it) }