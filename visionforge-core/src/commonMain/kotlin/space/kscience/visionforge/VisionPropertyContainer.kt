package space.kscience.visionforge

import space.kscience.dataforge.meta.Configurable
import space.kscience.dataforge.meta.MutableMeta
import space.kscience.dataforge.meta.ObservableMutableMeta
import space.kscience.dataforge.meta.get
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.values.Value

/**
 * Property containers are used to create a symmetric behaviors for vision properties and style builders
 */
public interface VisionPropertyContainer<out V : Vision> {

    public val meta: MutableMeta

    public fun getPropertyValue(
        name: Name,
        inherit: Boolean = false,
        includeStyles: Boolean = true,
        includeDefaults: Boolean = true,
    ): Value?
}

public open class SimpleVisionPropertyContainer<out V : Vision>(
    override val meta: ObservableMutableMeta,
) : VisionPropertyContainer<V>, Configurable {
    override fun getPropertyValue(
        name: Name,
        inherit: Boolean,
        includeStyles: Boolean,
        includeDefaults: Boolean
    ): Value? = meta[name]?.value
}