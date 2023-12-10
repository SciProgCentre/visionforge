package space.kscience.visionforge.solid.three

import space.kscience.visionforge.onPropertyChange
import space.kscience.visionforge.solid.PolyLine
import space.kscience.visionforge.solid.SolidMaterial
import space.kscience.visionforge.solid.color
import space.kscience.visionforge.solid.string
import space.kscience.visionforge.solid.three.ThreeMaterials.DEFAULT_LINE_COLOR
import three.core.BufferGeometry
import three.core.Object3D
import three.math.Color
import three.objects.LineSegments
import kotlin.math.ceil
import kotlin.reflect.KClass

public object ThreeLineFactory : ThreeFactory<PolyLine> {
    override val type: KClass<PolyLine> get() = PolyLine::class

    override suspend fun build(three: ThreePlugin, vision: PolyLine, observe: Boolean): Object3D {
        val geometry = BufferGeometry().apply {
            setFromPoints(Array((vision.points.size - 1) * 2) {
                vision.points[ceil(it / 2.0).toInt()].toVector()
            })
        }

        val material = ThreeMaterials.getLineMaterial(
            vision.properties[SolidMaterial.MATERIAL_KEY],
            false
        )

        material.linewidth = vision.thickness.toDouble()
        material.color = vision.color.string?.let { Color(it) } ?: DEFAULT_LINE_COLOR

        return LineSegments(geometry, material).apply {
            updatePosition(vision)
            //layers.enable(obj.layer)
            //add listener to object properties
            if(observe) {
                vision.onPropertyChange(three.context) { propertyName ->
                    updateProperty(vision, propertyName)
                }
            }
        }
    }

}