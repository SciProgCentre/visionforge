@file:UseSerializers(Point3DSerializer::class)

package hep.dataforge.vis.spatial

import hep.dataforge.io.serialization.ConfigSerializer
import hep.dataforge.meta.Config
import hep.dataforge.vis.common.AbstractVisualObject
import hep.dataforge.vis.common.number
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
class PolyLine(var points: List<Point3D>) : AbstractVisualObject(), VisualObject3D {
    @Serializable(ConfigSerializer::class)
    override var properties: Config? = null

    override var position: Point3D? = null
    override var rotation: Point3D? = null
    override var scale: Point3D? = null

    //var lineType by string()
    var thickness by number(1.0, key = "material.thickness")

}

fun VisualGroup3D.polyline(vararg points: Point3D, name: String = "", action: PolyLine.() -> Unit = {}) =
    PolyLine(points.toList()).apply(action).also { set(name, it) }