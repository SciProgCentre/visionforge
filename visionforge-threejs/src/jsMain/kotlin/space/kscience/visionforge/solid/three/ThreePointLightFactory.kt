package space.kscience.visionforge.solid.three

import space.kscience.dataforge.names.Name
import space.kscience.visionforge.onPropertyChange
import space.kscience.visionforge.solid.LightSource
import space.kscience.visionforge.solid.PointLightSource
import three.lights.PointLight
import three.math.Color
import kotlin.reflect.KClass

public object ThreePointLightFactory : ThreeFactory<PointLightSource> {
    override val type: KClass<in PointLightSource> get() = PointLightSource::class

    private val DEFAULT_COLOR = Color(0x404040)

    override suspend fun build(three: ThreePlugin, vision: PointLightSource, observe: Boolean): PointLight {
        val res = PointLight().apply {
            matrixAutoUpdate = false
            color = vision.color.threeColor() ?: DEFAULT_COLOR
            intensity = vision.intensity.toDouble()
            updatePosition(vision)
        }

        if (observe) {
            vision.onPropertyChange(three.context) { name, _ ->
                when (name) {
                    Name.of(LightSource::color.name) -> res.color = vision.color.threeColor() ?: DEFAULT_COLOR
                    Name.of(LightSource::intensity.name) -> res.intensity = vision.intensity.toDouble()
                    else -> res.updateProperty(vision, name)
                }
            }
        }

        return res
    }

}