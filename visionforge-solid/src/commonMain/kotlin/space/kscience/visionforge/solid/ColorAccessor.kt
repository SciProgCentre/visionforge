package space.kscience.visionforge.solid

import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.asName
import space.kscience.dataforge.names.plus
import space.kscience.dataforge.values.*
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

public fun Vision.color(): ReadOnlyProperty<Vision, ColorAccessor> = ReadOnlyProperty { _, property ->
    ColorAccessor(properties.root(), property.name.asName())
}

public var ColorAccessor?.string: String?
    get() = this?.value?.let { if (it == Null) null else it.string }
    set(value) {
        this?.value = value?.asValue()
    }

/**
 * Set [webcolor](https://en.wikipedia.org/wiki/Web_colors) as string
 */
public fun ColorAccessor?.set(webColor: String) {
    this?.value = webColor.asValue()
}

/**
 * Set color as RGB integer
 */
public fun ColorAccessor?.set(rgb: Int) {
    this?.value = Colors.rgbToString(rgb).asValue()
}

/**
 * Set color as RGB
 */
public fun ColorAccessor?.set(r: UByte, g: UByte, b: UByte) {
    this?.value = Colors.rgbToString(r, g, b).asValue()
}

public fun ColorAccessor?.clear() {
    this?.value = null
}