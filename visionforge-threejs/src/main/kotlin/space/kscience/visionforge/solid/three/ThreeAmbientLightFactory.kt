package space.kscience.visionforge.solid.three

import space.kscience.visionforge.solid.AmbientLightSource
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

        return res
    }
}