package space.kscience.visionforge

import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.MutableMeta
import space.kscience.dataforge.names.Name

/**
 * Property containers are used to create a symmetric behaviors for vision properties and style builders
 */
public interface VisionPropertyContainer<out V : Vision> {

    public fun getProperty(
        name: Name,
        inherit: Boolean,
        includeStyles: Boolean,
        includeDefaults: Boolean,
    ): Meta?
}

public open class SimpleVisionPropertyContainer<out V : Vision>(
    public val meta: MutableMeta,
) : VisionPropertyContainer<V> {
    override fun getProperty(
        name: Name,
        inherit: Boolean,
        includeStyles: Boolean,
        includeDefaults: Boolean,
    ): Meta? = meta.getMeta(name)
}