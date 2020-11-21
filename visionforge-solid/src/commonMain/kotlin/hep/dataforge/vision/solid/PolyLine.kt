package hep.dataforge.vision.solid

import hep.dataforge.meta.number
import hep.dataforge.names.Name
import hep.dataforge.names.asName
import hep.dataforge.names.plus
import hep.dataforge.vision.VisionContainerBuilder
import hep.dataforge.vision.props
import hep.dataforge.vision.set
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("solid.line")
public class PolyLine(public var points: List<Point3D>) : BasicSolid(), Solid {

    //var lineType by string()
    public var thickness: Number by props().number(1.0, key = SolidMaterial.MATERIAL_KEY + THICKNESS_KEY)

    public companion object {
        public val THICKNESS_KEY: Name = "thickness".asName()
    }

}

public fun VisionContainerBuilder<Solid>.polyline(
    vararg points: Point3D,
    name: String = "",
    action: PolyLine.() -> Unit = {},
): PolyLine =
    PolyLine(points.toList()).apply(action).also { set(name, it) }