package space.kscience.visionforge.solid.three

import info.laht.threekt.core.BufferGeometry
import info.laht.threekt.core.Object3D
import info.laht.threekt.math.Color
import info.laht.threekt.objects.LineSegments
import space.kscience.visionforge.onPropertyChange
import space.kscience.visionforge.solid.PolyLine
import space.kscience.visionforge.solid.SolidMaterial
import space.kscience.visionforge.solid.color
import space.kscience.visionforge.solid.string
import space.kscience.visionforge.solid.three.ThreeMaterials.DEFAULT_LINE_COLOR
import kotlin.math.ceil
import kotlin.reflect.KClass

public object ThreeLineFactory : ThreeFactory<PolyLine> {
    override val type: KClass<PolyLine> get() = PolyLine::class

    override fun build(three: ThreePlugin, obj: PolyLine): Object3D {
        val geometry = BufferGeometry().apply {
            setFromPoints(Array((obj.points.size - 1) * 2) {
                obj.points[ceil(it / 2.0).toInt()].toVector()
            })
        }

        val material = ThreeMaterials.getLineMaterial(
            obj.get(SolidMaterial.MATERIAL_KEY),
            false
        )

        material.linewidth = obj.thickness.toDouble()
        material.color = obj.color.string?.let { Color(it) } ?: DEFAULT_LINE_COLOR

        return LineSegments(geometry, material).apply {
            updatePosition(obj)
            //layers.enable(obj.layer)
            //add listener to object properties
            obj.onPropertyChange { propertyName ->
                updateProperty(obj, propertyName)
            }
        }
    }

}