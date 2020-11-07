package hep.dataforge.vision.solid.three

import hep.dataforge.meta.node
import hep.dataforge.vision.solid.PolyLine
import hep.dataforge.vision.solid.color
import hep.dataforge.vision.solid.three.ThreeMaterials.DEFAULT_LINE_COLOR
import info.laht.threekt.core.Geometry
import info.laht.threekt.core.Object3D
import info.laht.threekt.math.Color
import info.laht.threekt.objects.LineSegments
import kotlin.reflect.KClass

public object ThreeLineFactory : ThreeFactory<PolyLine> {
    override val type: KClass<PolyLine> get() = PolyLine::class

    override fun invoke(obj: PolyLine): Object3D {
        val geometry = Geometry().apply {
            vertices = obj.points.toTypedArray()
        }

        val material = ThreeMaterials.getLineMaterial(obj.getItem(MeshThreeFactory.EDGES_MATERIAL_KEY).node, true)

        material.linewidth = obj.thickness.toDouble()
        material.color = obj.color?.let { Color(it) } ?: DEFAULT_LINE_COLOR

        return LineSegments(geometry, material).apply {
            updatePosition(obj)
            //layers.enable(obj.layer)
            //add listener to object properties
            obj.onPropertyChange(this) { propertyName->
                updateProperty(obj, propertyName)
            }
        }
    }

}