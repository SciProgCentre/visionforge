package space.kscience.visionforge.solid

import space.kscience.dataforge.meta.*
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.asName
import space.kscience.dataforge.names.plus
import space.kscience.visionforge.Colors
import space.kscience.visionforge.Vision
import space.kscience.visionforge.VisionBuilder
import space.kscience.visionforge.root
import kotlin.properties.ReadOnlyProperty

@VisionBuilder
public class ColorAccessor(
    private val provider: MutableValueProvider,
    private val colorKey: Name,
) : MutableValueProvider {
    public var value: Value?
        get() = provider.getValue(colorKey)
        set(value) {
            provider.setValue(colorKey, value)
        }

    override fun getValue(name: Name): Value? = provider.getValue(colorKey + name)

    override fun setValue(name: Name, value: Value?) {
        provider.setValue(colorKey + name, value)
    }
}

public fun Vision.colorProperty(
    propertyName: Name? = null,
): ReadOnlyProperty<Vision, ColorAccessor> = ReadOnlyProperty { _, property ->
    ColorAccessor(properties.root(true), propertyName ?: property.name.asName())
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