package space.kscience.visionforge.solid

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.dataforge.meta.ValueType
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.dataforge.meta.descriptors.node
import space.kscience.dataforge.meta.descriptors.value
import space.kscience.dataforge.meta.number
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.asName
import space.kscience.visionforge.*

@Serializable
public abstract class LightSource : MiscSolid() {
    override val descriptor: MetaDescriptor get() = LightSource.descriptor

    public val color: ColorAccessor by colorProperty(SolidMaterial.COLOR_KEY)
    public var intensity: Number by properties.root(includeStyles = false).number(INTENSITY_KEY) { 1.0 }

    public companion object {
        public val INTENSITY_KEY: Name = "intensity".asName()

        public val descriptor: MetaDescriptor by lazy {
            MetaDescriptor {
                value(Vision.VISIBLE_KEY, ValueType.BOOLEAN) {
                    inherited = false
                    default(true)
                }

                value(LightSource::color.name, ValueType.STRING, ValueType.NUMBER) {
                    inherited = false
                    widgetType = "color"
                    default(Colors.white)
                }

                value(LightSource::intensity.name, ValueType.NUMBER) {
                    inherited = false
                    default(1.0)
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
): AmbientLightSource = AmbientLightSource().apply(block).also { setChild(name, it) }

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
    it.position = Float32Vector3D(x, y, z)
    setChild(name, it)
}