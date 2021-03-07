package space.kscience.visionforge

import space.kscience.dataforge.meta.MetaItem
import space.kscience.dataforge.names.Name

/**
 * Property containers are used to create a symmetric behaviors for vision properties and style builders
 */
public interface VisionPropertyContainer<out T> {
    public fun getProperty(
        name: Name,
        inherit: Boolean = false,
        includeStyles: Boolean = true,
        includeDefaults: Boolean = true,
    ): MetaItem?

    public fun setProperty(name: Name, item: MetaItem?, notify: Boolean = true)
}