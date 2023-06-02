package space.kscience.visionforge.solid.three

import space.kscience.dataforge.names.Name
import space.kscience.visionforge.Vision
import space.kscience.visionforge.onPropertyChange
import space.kscience.visionforge.solid.AmbientLightSource
import space.kscience.visionforge.solid.LightSource
import space.kscience.visionforge.solid.SolidMaterial
import space.kscience.visionforge.visible
import three.lights.AmbientLight
import three.math.Color
import kotlin.reflect.KClass

public object ThreeAmbientLightFactory : ThreeFactory<AmbientLightSource> {
    override val type: KClass<in AmbientLightSource> get() = AmbientLightSource::class

    override fun build(three: ThreePlugin, vision: AmbientLightSource, observe: Boolean): AmbientLight {
        val res = AmbientLight().apply {
            color = vision.color.threeColor() ?: Color(0x404040)
            intensity = vision.intensity.toDouble()
        }

        if (observe) {
            vision.onPropertyChange(three.context) { propertyName: Name ->
                when (propertyName) {
                    Vision.VISIBLE_KEY -> res.visible = vision.visible ?: true
                    SolidMaterial.COLOR_KEY -> res.color = vision.color.threeColor() ?: Color(0x404040)
                    LightSource.INTENSITY_KEY -> res.intensity = vision.intensity.toDouble()
                }
            }
        }

        return res
    }
}