package space.kscience.visionforge.solid

import space.kscience.dataforge.meta.*
import space.kscience.dataforge.names.Name
import space.kscience.visionforge.Colors
import space.kscience.visionforge.MutableVision
import space.kscience.visionforge.Vision
import space.kscience.visionforge.VisionBuilder
import kotlin.properties.ReadOnlyProperty

/**
 * A property that provides access to a color value
 */
@VisionBuilder
public class ColorAccessor(
    private val provider: MutableMeta,
) : MutableValueProvider {
    public var value: Value?
        get() = provider.value
        set(value) {
            provider.value = value
        }

    override fun getValue(name: Name): Value? = provider.getValue(name)

    override fun setValue(name: Name, value: Value?) {
        provider.setValue(name, value)
    }
}

public fun MutableVision.colorProperty(
    propertyName: Name? = null,
): ReadOnlyProperty<Vision, ColorAccessor> = ReadOnlyProperty { _, property ->
    ColorAccessor(mutableProperty(propertyName ?: Name.of(property.name), inherited = true))
}

public var ColorAccessor.string: String?
    get() = value?.let { if (it == Null) null else it.string }
    set(value) {
        this.value = value?.asValue()
    }

/**
 * Set [webcolor](https://en.wikipedia.org/wiki/Web_colors) as string
 */
public operator fun ColorAccessor.invoke(webColor: String) {
    value = webColor.asValue()
}

/**
 * Set color as RGB integer
 */
public operator fun ColorAccessor.invoke(rgb: Int) {
    value = Colors.rgbToString(rgb).asValue()
}

/**
 * Set color as RGB
 */
public operator fun ColorAccessor.invoke(r: UByte, g: UByte, b: UByte) {
    value = Colors.rgbToString(r, g, b).asValue()
}

public fun ColorAccessor.clear() {
    value = null
}