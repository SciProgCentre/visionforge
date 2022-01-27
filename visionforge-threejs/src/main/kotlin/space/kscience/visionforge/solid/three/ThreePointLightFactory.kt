package space.kscience.visionforge.solid.three

import info.laht.threekt.lights.PointLight
import info.laht.threekt.math.Color
import space.kscience.visionforge.onPropertyChange
import space.kscience.visionforge.solid.PointLightSource
import kotlin.reflect.KClass

public object ThreePointLightFactory : ThreeFactory<PointLightSource> {
    override val type: KClass<in PointLightSource> get() = PointLightSource::class

    override fun invoke(three: ThreePlugin, obj: PointLightSource): PointLight {
        val res = PointLight().apply {
            matrixAutoUpdate = false
            color = obj.color.threeColor() ?: Color(0x404040)
            intensity = obj.intensity.toDouble()
            updatePosition(obj)
        }

        obj.onPropertyChange { name ->
            when {
                else -> res.updateProperty(obj, name)
            }
        }

        return res
    }
}