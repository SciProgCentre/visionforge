package space.kscience.visionforge.solid.three

import info.laht.threekt.lights.PointLight
import info.laht.threekt.math.Color
import space.kscience.dataforge.names.asName
import space.kscience.visionforge.onPropertyChange
import space.kscience.visionforge.solid.LightSource
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
            when (name) {
                LightSource::color.name.asName() -> res.color = obj.color.threeColor() ?: Color(0x404040)
                LightSource::intensity.name.asName() -> res.intensity = obj.intensity.toDouble()
                else -> res.updateProperty(obj, name)
            }
        }

        return res
    }
}