package hep.dataforge.vision.solid

import hep.dataforge.meta.MutableItemProvider
import hep.dataforge.meta.set
import hep.dataforge.meta.value
import hep.dataforge.names.Name
import hep.dataforge.values.Value
import hep.dataforge.values.asValue
import hep.dataforge.values.string
import hep.dataforge.vision.Colors
import hep.dataforge.vision.VisionBuilder

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