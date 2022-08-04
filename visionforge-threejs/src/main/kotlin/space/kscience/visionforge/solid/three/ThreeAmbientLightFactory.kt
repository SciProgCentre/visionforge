package space.kscience.visionforge.solid.three

import info.laht.threekt.lights.AmbientLight
import info.laht.threekt.math.Color
import space.kscience.visionforge.solid.AmbientLightSource
import kotlin.reflect.KClass

public object ThreeAmbientLightFactory : ThreeFactory<AmbientLightSource> {
    override val type: KClass<in AmbientLightSource> get() = AmbientLightSource::class

    override fun build(three: ThreePlugin, obj: AmbientLightSource): AmbientLight {
        val res = AmbientLight().apply {
            color = obj.color.threeColor() ?: Color(0x404040)
            intensity = obj.intensity.toDouble()
        }

        return res
    }
}