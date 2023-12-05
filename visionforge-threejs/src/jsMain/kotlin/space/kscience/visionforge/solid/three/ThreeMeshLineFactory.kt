package space.kscience.visionforge.solid.three

import space.kscience.visionforge.onPropertyChange
import space.kscience.visionforge.solid.PolyLine
import space.kscience.visionforge.solid.color
import space.kscience.visionforge.solid.string
import three.core.Object3D
import three.math.Color
import three.meshline.MeshLine
import three.meshline.MeshLineMaterial
import three.objects.Mesh
import kotlin.math.ceil
import kotlin.reflect.KClass

public object ThreeMeshLineFactory : ThreeFactory<PolyLine> {
    override val type: KClass<in PolyLine> get() = PolyLine::class

    override suspend fun build(three: ThreePlugin, vision: PolyLine, observe: Boolean): Object3D {
        val geometry = MeshLine(
            Array((vision.points.size - 1) * 2) {
                vision.points[ceil(it / 2.0).toInt()].toVector()
            }
        )

        val material = MeshLineMaterial().apply {
            thickness = vision.thickness.toFloat()
            color = vision.color.string?.let { Color(it) } ?: ThreeMaterials.DEFAULT_LINE_COLOR
        }

        return Mesh(geometry, material).apply {
            updatePosition(vision)
            //layers.enable(obj.layer)
            //add listener to object properties
            if (observe) {
                vision.onPropertyChange(three.context) { propertyName ->
                    updateProperty(vision, propertyName)
                }
            }
        }
    }
}