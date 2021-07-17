package space.kscience.visionforge.solid

import space.kscience.dataforge.meta.MutableItemProvider
import space.kscience.dataforge.meta.set
import space.kscience.dataforge.meta.value
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.values.Value
import space.kscience.dataforge.values.asValue
import space.kscience.dataforge.values.string
import space.kscience.visionforge.Colors
import space.kscience.visionforge.VisionBuilder

@VisionBuilder
public class ColorAccessor(private val parent: MutableItemProvider, private val colorKey: Name) {
    public var value: Value?
        get() = parent.getItem(colorKey).value
        set(value) {
            parent[colorKey] = value
        }
}

public var ColorAccessor?.string: String?
    get() = this?.value?.string
    set(value) {
        this?.value = value?.asValue()
    }

/**
 * Set [webcolor](https://en.wikipedia.org/wiki/Web_colors) as string
 */
public operator fun ColorAccessor?.invoke(webColor: String) {
    this?.value = webColor.asValue()
}

/**
 * Set color as RGB integer
 */
public operator fun ColorAccessor?.invoke(rgb: Int) {
    this?.value = Colors.rgbToString(rgb).asValue()
}

/**
 * Set color as RGB
 */
public operator fun ColorAccessor?.invoke(r: UByte, g: UByte, b: UByte) {
    this?.value = Colors.rgbToString(r, g, b).asValue()
}

public fun ColorAccessor?.clear() {
    this?.value = null
}