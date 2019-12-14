@file:UseSerializers(Point3DSerializer::class)

package hep.dataforge.vis.spatial

import hep.dataforge.io.serialization.ConfigSerializer
import hep.dataforge.meta.Config
import hep.dataforge.vis.common.AbstractVisualObject
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
class Text3D(var text: String, var fontSize: Int) : AbstractVisualObject(), VisualObject3D {
    @Serializable(ConfigSerializer::class)
    override var properties: Config? = null

    override var position: Point3D? = null
    override var rotation: Point3D? = null
    override var scale: Point3D? = null

}

fun VisualGroup3D.text(text: String, fontSize: Int, name: String = "", action: Text3D.() -> Unit = {}) =
    Text3D(text, fontSize).apply(action).also { set(name, it) }