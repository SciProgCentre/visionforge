package space.kscience.visionforge.solid.three


import three.core.Object3D
import three.geometries.TextBufferGeometry
import three.objects.Mesh
import kotlinx.js.jso
import space.kscience.dataforge.context.logger
import space.kscience.dataforge.context.warn
import space.kscience.visionforge.onPropertyChange
import space.kscience.visionforge.solid.SolidLabel
import kotlin.reflect.KClass

/**
 *
 */
public object ThreeLabelFactory : ThreeFactory<SolidLabel> {
    override val type: KClass<in SolidLabel> get() = SolidLabel::class

    override fun build(three: ThreePlugin, vision: SolidLabel, observe: Boolean): Object3D {
        val textGeo = TextBufferGeometry(vision.text, jso {
            font = vision.fontFamily
            size = 20
            height = 1
            curveSegments = 1
        })
        return Mesh(textGeo, ThreeMaterials.DEFAULT).apply {
            createMaterial(vision)
            updatePosition(vision)
            if(observe) {
                vision.onPropertyChange(three.context) {
                    //TODO
                    three.logger.warn { "Label parameter change not implemented" }
                }
            }
        }
    }
}