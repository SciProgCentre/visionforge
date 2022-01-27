package space.kscience.visionforge.solid

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import space.kscience.dataforge.names.asName
import space.kscience.visionforge.VisionBuilder
import space.kscience.visionforge.VisionContainerBuilder
import space.kscience.visionforge.numberProperty
import space.kscience.visionforge.set

@Serializable
public abstract class LightSource : SolidBase() {
    @Transient
    public val color: ColorAccessor = ColorAccessor(meta, "color".asName())
    public var intensity: Number by numberProperty(includeStyles = false) { 1.0 }
}

@Serializable
@SerialName("solid.light.ambient")
public class AmbientLightSource : LightSource()

@VisionBuilder
public fun VisionContainerBuilder<Solid>.ambientLight(
    name: String? = "@ambientLight",
    block: AmbientLightSource.() -> Unit = {},
): AmbientLightSource = AmbientLightSource().apply(block).also { set(name, it) }

@Serializable
@SerialName("solid.light.point")
public class PointLightSource : LightSource()


@VisionBuilder
public fun VisionContainerBuilder<Solid>.pointLight(
    x: Number,
    y: Number,
    z: Number,
    name: String? = null,
    block: PointLightSource.() -> Unit = {},
): PointLightSource = PointLightSource().apply(block).also {
    it.position = Point3D(x, y, z)
    set(name, it)
}