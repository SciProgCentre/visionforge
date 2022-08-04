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

    private val DEFAULT_COLOR = Color(0x404040)

    override fun build(three: ThreePlugin, obj: PointLightSource): PointLight {
        val res = PointLight().apply {
            matrixAutoUpdate = false
            color = obj.color.threeColor() ?: DEFAULT_COLOR
            intensity = obj.intensity.toDouble()
            updatePosition(obj)
        }

        obj.onPropertyChange { name ->
            when (name) {
                LightSource::color.name.asName() -> res.color = obj.color.threeColor() ?: DEFAULT_COLOR
                LightSource::intensity.name.asName() -> res.intensity = obj.intensity.toDouble()
                else -> res.updateProperty(obj, name)
            }
        }

        return res
    }

}