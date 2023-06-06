package space.kscience.visionforge.solid.three

import space.kscience.visionforge.onPropertyChange
import space.kscience.visionforge.solid.AxesSolid
import three.helpers.AxesHelper
import kotlin.reflect.KClass

public object ThreeAxesFactory : ThreeFactory<AxesSolid> {
    override val type: KClass<in AxesSolid> get() = AxesSolid::class

    override suspend fun build(three: ThreePlugin, vision: AxesSolid, observe: Boolean): AxesHelper {
        val res = AxesHelper(vision.size.toInt())

        if (observe) {
            vision.onPropertyChange(three.context) { propertyName ->
                res.updateProperty(vision, propertyName)
            }
        }

        return res
    }
}