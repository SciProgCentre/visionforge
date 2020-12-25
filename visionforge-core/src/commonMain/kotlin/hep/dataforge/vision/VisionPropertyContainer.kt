package hep.dataforge.vision

import hep.dataforge.meta.MetaItem
import hep.dataforge.names.Name

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