package space.kscience.visionforge.solid

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.dataforge.meta.descriptors.node
import space.kscience.dataforge.meta.descriptors.value
import space.kscience.dataforge.meta.number
import space.kscience.dataforge.values.ValueType
import space.kscience.visionforge.*

@Serializable
public abstract class LightSource : SolidBase<LightSource>() {
    override val descriptor: MetaDescriptor get() = LightSource.descriptor

    public val color: ColorAccessor by color()
    public var intensity: Number by properties.root(includeStyles = false).number { 1.0 }

    public companion object{
        public val descriptor: MetaDescriptor by lazy {
            MetaDescriptor {
                value(Vision.VISIBLE_KEY, ValueType.BOOLEAN) {
                    inherited = false
                    default(true)
                }

                value(LightSource::color.name, ValueType.STRING, ValueType.NUMBER) {
                    inherited = false
                    widgetType = "color"
                }

                value(LightSource::intensity.name, ValueType.NUMBER) {
                    inherited = false
                    default(1.0)
                }

                value(SolidMaterial.COLOR_KEY, ValueType.STRING, ValueType.NUMBER) {
                    inherited = false
                    widgetType = "color"
                }

                node(Solid.POSITION_KEY) {
                    hide()
                }
            }
        }
    }
}

@Serializable
@SerialName("solid.light.ambient")
public class AmbientLightSource : LightSource()

@VisionBuilder
public fun MutableVisionContainer<Solid>.ambientLight(
    name: String? = "@ambientLight",
    block: AmbientLightSource.() -> Unit = {},
): AmbientLightSource = AmbientLightSource().apply(block).also { set(name, it) }

@Serializable
@SerialName("solid.light.point")
public class PointLightSource : LightSource()


@VisionBuilder
public fun MutableVisionContainer<Solid>.pointLight(
    x: Number,
    y: Number,
    z: Number,
    name: String? = null,
    block: PointLightSource.() -> Unit = {},
): PointLightSource = PointLightSource().apply(block).also {
    it.position = Point3D(x, y, z)
    set(name, it)
}